package org.jpmml.evaluator;

public class EvaluationRequest {
  private String key; // key of pmml file
  private String csvData;

  public EvaluationRequest() {}

  public EvaluationRequest(String key, String csvData) {
    this.key = key;
    this.csvData = csvData;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getCsvData() {
    return csvData;
  }

  public void setCsvData(String csvData) {
    this.csvData = csvData;
  }
}