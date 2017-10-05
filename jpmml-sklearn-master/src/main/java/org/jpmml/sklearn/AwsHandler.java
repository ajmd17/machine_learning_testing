package org.jpmml.sklearn;


import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.bind.JAXBException;

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
import org.jpmml.model.ImportFilter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.jpmml.model.MetroJAXBUtil;
import sklearn.Estimator;
import sklearn.pipeline.Pipeline;
import sklearn2pmml.PMMLPipeline;


public class AwsHandler implements RequestHandler<SKLearnRequest, SKLearnResponse> {
  @Override
  public SKLearnResponse handleRequest(SKLearnRequest input, Context context) {
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

    PMML pmml;
    
    try {
      pmml = this.run(dataInputStream);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to convert sklearn model to pmml: " + ex.getMessage());
    }

    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {
      MetroJAXBUtil.marshalPMML(pmml, os);
    } catch (JAXBException ex) {
      throw new RuntimeException("Failed to marshal pmml: " + ex.getMessage());
    }

    final byte[] buffer = os.toByteArray();
    final InputStream is = new ByteArrayInputStream(buffer);

    final ObjectMetadata meta = new ObjectMetadata();
    meta.setContentLength(buffer.length);

    final String key = "pmmlmodels/" + System.currentTimeMillis() + ".pmml";
    s3.putObject(new PutObjectRequest(bucket, key, is, meta));

    return new SKLearnResponse(key);
  }

  public PMML run(InputStream inputStream) throws Exception {
		Object object;

		try (Storage storage = PickleUtil.createStorage(inputStream)) {
			object = PickleUtil.unpickle(storage);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to unpickle object: " + ex.getMessage());
		}

		if(!(object instanceof PMMLPipeline)){

			// Create a single- or multi-step PMMLPipeline from a Pipeline
			if(object instanceof Pipeline){
				Pipeline pipeline = (Pipeline)object;

				object = new PMMLPipeline()
					.setSteps(pipeline.getSteps());
			} else

			// Create a single-step PMMLPipeline from an Estimator
			if(object instanceof Estimator){
				Estimator estimator = (Estimator)object;

				object = new PMMLPipeline()
					.setSteps(Collections.<Object[]>singletonList(new Object[]{"estimator", estimator}));
			} else

			{
				throw new IllegalArgumentException("The object (" + ClassDictUtil.formatClass(object) + ") is not a PMMLPipeline");
			}
		}

		PMMLPipeline pipeline = (PMMLPipeline)object;

		PMML pmml;

		try {
			pmml = pipeline.encodePMML();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to encode PMML: " + ex.getMessage());
		}

    return pmml;
	}
}