package org.schemaextractor;

public class PmmlS3Response {
  PmmlSchema schema;

  public PmmlS3Response(PmmlSchema schema) {
    this.schema = schema;
  }

  public PmmlSchema getSchema() {
    return schema;
  }
}