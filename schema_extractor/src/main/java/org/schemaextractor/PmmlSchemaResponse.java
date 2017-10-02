package org.schemaextractor;

import java.util.HashMap;
import java.util.ArrayList;


public class PmmlSchemaResponse {
  HashMap<String, PmmlField> shape;

  public PmmlSchemaResponse(HashMap<String, PmmlField> shape) {
    this.shape = shape;
  }

  public HashMap<String, PmmlField> getShape() {
    return shape;
  }

  static class PmmlField {
    String opType;
    String dataType;
    ArrayList<String> possibleValues;

    PmmlField(String opType, String dataType) {
      this.opType = opType;
      this.dataType = dataType;
      this.possibleValues = null;
    }

    PmmlField(String opType, String dataType, ArrayList<String> possibleValues) {
      this.opType = opType;
      this.dataType = dataType;
      this.possibleValues = possibleValues;
    }

    public void addPossibleValue(String value) {
      if (possibleValues == null) {
        possibleValues = new ArrayList<String>();
      }

      possibleValues.add(value);
    }

    public String getOpType() {
      return opType;
    }

    public void setOpType(String opType) {
      this.opType = opType;
    }

    public String getDataType() {
      return dataType;
    }

    public void setDataType(String dataType) {
      this.dataType = dataType;
    }

    public ArrayList<String> getPossibleValues() {
      return possibleValues;
    }

    public void setPossibleValues(ArrayList<String> possibleValues) {
      this.possibleValues = possibleValues;
    }
  }
}