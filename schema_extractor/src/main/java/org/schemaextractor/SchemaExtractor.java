package org.jpmml.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.dmg.pmml.PMML;


public class SchemaExtractor {

    @Parameter(names = {
        "--input"
    }, description = "Input PMML file", required = true)
    private File input = null;

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

    public PMML unmarshalPMML(File file) throws JAXBException, SAXException, IOException {
        Unmarshaller unmarshaller = createUnmarshaller();

        try (InputStream is = new FileInputStream(file)) {
            Source source = ImportFilter.apply(new InputSource(is));
            return (PMML) unmarshaller.unmarshal(source);
        }
    }

    public Marshaller createMarshaller() throws JAXBException {
        return JAXBUtil.createMarshaller();
    }

    public void marshalPMML(PMML pmml, File file) throws JAXBException, IOException {
        Marshaller marshaller = createMarshaller();

        try (OutputStream os = new FileOutputStream(file)) {
            Result result = new StreamResult(os);
            marshaller.marshal(pmml, result);
        }
    }

    public void execute() {
        PMML pmml;

        try {
            pmml = unmarshalPMML(this.input);

            // create json schema obj
            JsonBuilderFactory factory = Json.createBuilderFactory(null);
            JsonObjectBuilder json = factory.createObjectBuilder();
            JsonObjectBuilder fields = factory.createObjectBuilder();

            for (org.dmg.pmml.DataField df: pmml.getDataDictionary().getDataFields()) {
                JsonObjectBuilder field = factory.createObjectBuilder();
                JsonArrayBuilder values = factory.createArrayBuilder();

                for (org.dmg.pmml.Value dv: df.getValues()) {
                    values.add(dv.getValue());
                }

                field.add("values", values);
                field.add("opType", df.getOpType().value());
                field.add("dataType", df.getDataType().value());
                fields.add(df.getName().getValue(), field);
            }

            json.add("fields", fields);
            JsonObject jsonObject = json.build();
            System.out.println(jsonObject.toString());
        } catch (JAXBException | SAXException | IOException ex) {
            SchemaExtractor.logger.log(Level.SEVERE, ex.toString());
        }
    }

    public class SimpleValidationEventHandler implements ValidationEventHandler {
        @Override
        public boolean handleEvent(ValidationEvent event) {
            Level level = (event.getSeverity() == ValidationEvent.WARNING ? Level.WARNING : Level.SEVERE);
            SchemaExtractor.logger.log(level, String.valueOf(event));
            return true;
        }
    }

    public static void main(String...args) {
        SchemaExtractor schemaExtractor = new SchemaExtractor();
        JCommander commander = new JCommander(schemaExtractor);
        commander.setProgramName(schemaExtractor.getClass().getName());

        try {
            commander.parse(args);
        } catch (ParameterException pe) {
            commander.usage();
            System.exit(-1);
        }

        schemaExtractor.execute();
    }

    private static final Logger logger = Logger.getLogger(SchemaExtractor.class.getName());
}
