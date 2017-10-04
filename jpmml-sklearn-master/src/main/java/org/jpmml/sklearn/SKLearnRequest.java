package org.jpmml.sklearn;

public class SKLearnRequest {
  private String key; // key of the pickle file

  public SKLearnRequest(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}