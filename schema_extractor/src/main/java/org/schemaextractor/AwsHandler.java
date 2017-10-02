package org.schemaextractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.bind.JAXBException;
import javax.json.JsonObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.Source;

import org.dmg.pmml.PMML;
import org.jpmml.model.ImportFilter;

import org.schemaextractor.SchemaExtractor;


public class AwsHandler implements RequestHandler<PmmlS3Request, PmmlSchemaResponse> {
  @Override
  public PmmlSchemaResponse handleRequest(PmmlS3Request input, Context context) {
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
    
    SchemaExtractor se = new SchemaExtractor();

    try {
      Unmarshaller unmarshaller = se.createUnmarshaller();
      Source source = ImportFilter.apply(new InputSource(dataInputStream));
      PMML pmml = (PMML) unmarshaller.unmarshal(source);

      return se.createSchemaFromPmml(pmml);

      /*JsonObject jsonObject = se.createJsonFromPmml(pmml);

      return jsonObject.toString();*/
    } catch (JAXBException | SAXException ex) {
      throw new RuntimeException("Failed to unmarshall PMML data");
    }
  }
}