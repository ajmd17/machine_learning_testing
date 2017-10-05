package org.jpmml.evaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import javax.xml.validation.Schema;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.Source;

import org.dmg.pmml.PMML;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Visitor;
import org.jpmml.model.ImportFilter;
import org.jpmml.evaluator.visitors.ExpressionOptimizer;
import org.jpmml.evaluator.visitors.FieldOptimizer;
import org.jpmml.evaluator.visitors.GeneralRegressionModelOptimizer;
import org.jpmml.evaluator.visitors.MiningFieldInterner;
import org.jpmml.evaluator.visitors.NaiveBayesModelOptimizer;
import org.jpmml.evaluator.visitors.PredicateInterner;
import org.jpmml.evaluator.visitors.PredicateOptimizer;
import org.jpmml.evaluator.visitors.RegressionModelOptimizer;
import org.jpmml.evaluator.visitors.ScoreDistributionInterner;
import org.jpmml.model.visitors.ArrayListOptimizer;
import org.jpmml.model.visitors.ArrayListTransformer;
import org.jpmml.model.visitors.DoubleInterner;
import org.jpmml.model.visitors.IntegerInterner;
import org.jpmml.model.visitors.StringInterner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jpmml.model.JAXBUtil;

public class AwsHandler implements RequestHandler<EvaluationRequest, EvaluationResponse> {
  static final boolean COPY_COLUMNS = false;
  static final boolean PERMIT_MISSING_FIELDS = true;
  static final boolean ENABLE_OPTIMIZATIONS = true;
  static final boolean ENABLE_INTERNING = true;

  public Unmarshaller createUnmarshaller() throws JAXBException {
      Unmarshaller unmarshaller = JAXBUtil.createUnmarshaller();
      Schema schema;

      try {
          schema = JAXBUtil.getSchema();
      } catch (Exception e) {
          throw new RuntimeException(e);
      }

      unmarshaller.setSchema(schema);
      unmarshaller.setEventHandler(new SimpleValidationEventHandler());

      return unmarshaller;
  }

  @Override
  public EvaluationResponse handleRequest(EvaluationRequest input, Context context) {
    String bucket = "mlmodeltestingbucket";

    InputStream dataInputStream;

    AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    try {
        S3Object o = s3.getObject(bucket, input.getKey());
        S3ObjectInputStream s3is = o.getObjectContent();
        dataInputStream = s3is;
    } catch (Exception ex) {
      throw new RuntimeException("Failed to load data from storage");
    }

    if (dataInputStream == null) {
      throw new NullPointerException("dataInputStream is null");
    }

    PMML pmml = null;
    
    try {
      Unmarshaller unmarshaller = createUnmarshaller();
      Source source = ImportFilter.apply(new InputSource(dataInputStream));
      pmml = (PMML) unmarshaller.unmarshal(source);
    } catch (JAXBException | SAXException ex) {
      throw new RuntimeException("Failed to unmarshall PMML data: " + ex.getMessage());
    }

    CsvUtil.Table inputTable;

    try {
        InputStream is = new ByteArrayInputStream(input.getCsvData().getBytes("UTF-8"));
        inputTable = CsvUtil.readTable(is);
    } catch (java.io.UnsupportedEncodingException ex) {
      throw new RuntimeException("Unsupported encoding: " + ex.getMessage());
    } catch (java.io.IOException ex) {
      throw new RuntimeException("Failed to read CSV table: " + ex.getMessage());
    }

		List<? extends Map<FieldName, ?>> inputRecords = BatchUtil.parseRecords(inputTable, Example.CSV_PARSER);

    if (AwsHandler.ENABLE_OPTIMIZATIONS) {
			List<? extends Visitor> optimizers = Arrays.asList(new ArrayListOptimizer(), new ExpressionOptimizer(), new FieldOptimizer(), new PredicateOptimizer(), new GeneralRegressionModelOptimizer(), new NaiveBayesModelOptimizer(), new RegressionModelOptimizer());

			for (Visitor optimizer : optimizers){
				optimizer.applyTo(pmml);
			}
		} // End if

		// Optimize first, intern second.
		// The goal is to intern optimized elements (keeps one copy), not optimize interned elements (expands one copy to multiple copies).
		if (AwsHandler.ENABLE_INTERNING) {
			List<? extends Visitor> interners = Arrays.asList(new DoubleInterner(), new IntegerInterner(), new StringInterner(), new MiningFieldInterner(), new PredicateInterner(), new ScoreDistributionInterner());

			for (Visitor interner : interners){
				interner.applyTo(pmml);
			}
		} // End if

		if (AwsHandler.ENABLE_OPTIMIZATIONS || AwsHandler.ENABLE_INTERNING){
			Visitor transformer = new ArrayListTransformer();

			transformer.applyTo(pmml);
		}

		ModelEvaluatorFactory modelEvaluatorFactory = new ModelEvaluatorFactory();
		modelEvaluatorFactory.setValueFactoryFactory(new ValueFactoryFactory());

		Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);

		// Perform self-testing
		evaluator.verify();

		List<InputField> inputFields = evaluator.getInputFields();
		List<InputField> groupFields = Collections.emptyList();

		if (evaluator instanceof HasGroupFields){
			HasGroupFields hasGroupfields = (HasGroupFields)evaluator;

			groupFields = hasGroupfields.getGroupFields();
		} // End if

		if (inputRecords.size() > 0){
			Map<FieldName, ?> inputRecord = inputRecords.get(0);

			Sets.SetView<FieldName> missingInputFields = Sets.difference(new LinkedHashSet<>(EvaluatorUtil.getNames(inputFields)), inputRecord.keySet());
			if ((missingInputFields.size() > 0) && !AwsHandler.PERMIT_MISSING_FIELDS) {
				throw new IllegalArgumentException("Missing input field(s): " + missingInputFields.toString());
			}

			Sets.SetView<FieldName> missingGroupFields = Sets.difference(new LinkedHashSet<>(EvaluatorUtil.getNames(groupFields)), inputRecord.keySet());
			if (missingGroupFields.size() > 0) {
				throw new IllegalArgumentException("Missing group field(s): " + missingGroupFields.toString());
			}
		} // End if

		if (evaluator instanceof HasGroupFields){
			HasGroupFields hasGroupFields = (HasGroupFields)evaluator;

			inputRecords = EvaluatorUtil.groupRows(hasGroupFields, inputRecords);
		}

		List<Map<FieldName, ?>> outputRecords = new ArrayList<>(inputRecords.size());
    outputRecords.clear();

    Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

    for(Map<FieldName, ?> inputRecord : inputRecords){
      arguments.clear();

      for (InputField inputField : inputFields){
        FieldName name = inputField.getName();

        FieldValue value = EvaluatorUtil.prepare(inputField, inputRecord.get(name));

        arguments.put(name, value);
      }

      Map<FieldName, ?> result = evaluator.evaluate(arguments);

      outputRecords.add(result);
    }

		List<TargetField> targetFields = evaluator.getTargetFields();
		List<OutputField> outputFields = evaluator.getOutputFields();

		List<? extends ResultField> resultFields = Lists.newArrayList(Iterables.concat(targetFields, outputFields));

		CsvUtil.Table outputTable = new CsvUtil.Table();
		outputTable.setSeparator(inputTable.getSeparator());

		outputTable.addAll(BatchUtil.formatRecords(outputRecords, EvaluatorUtil.getNames(resultFields), Example.CSV_FORMATTER));

		if ((inputTable.size() == outputTable.size()) && AwsHandler.COPY_COLUMNS){

			for(int i = 0; i < inputTable.size(); i++){
				List<String> inputRow = inputTable.get(i);
				List<String> outputRow = outputTable.get(i);

				outputRow.addAll(0, inputRow);
			}
		}

    ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {
      CsvUtil.writeTable(outputTable, os);
    } catch (java.io.IOException ex) {
      throw new RuntimeException("Failed to write CSV table: " + ex.getMessage());
    }

    return new EvaluationResponse(os.toString());
  }

  public class SimpleValidationEventHandler implements ValidationEventHandler {
      @Override
      public boolean handleEvent(ValidationEvent event) {
          Level level = (event.getSeverity() == ValidationEvent.WARNING ? Level.WARNING : Level.SEVERE);
          AwsHandler.logger.log(level, String.valueOf(event));
          return true;
      }
  }

  private static final Logger logger = Logger.getLogger(AwsHandler.class.getName());
}