package org.jpmml.evaluator;

public class EvaluationResponse {
  private String csvData;

  public EvaluationResponse() {}

  public EvaluationResponse(String csvData) {
    this.csvData = csvData;
  }

  public String getCsvData() {
    return csvData;
  }

  public void setCsvData(String csvData) {
    this.csvData = csvData;
  }
}