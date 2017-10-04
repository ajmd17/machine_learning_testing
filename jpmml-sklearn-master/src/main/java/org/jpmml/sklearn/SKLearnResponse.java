package org.jpmml.sklearn;

public class SKLearnResponse {
  private String key; // key of the pmml file

  public SKLearnResponse(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}