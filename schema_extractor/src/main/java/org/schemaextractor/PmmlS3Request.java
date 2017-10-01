package org.schemaextractor;

public class PmmlS3Request {
    String key;

    public PmmlS3Request(String key) {
        this.key = key;
    }

    public PmmlS3Request() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "PmmlS3Request { key: " + key + " }";
    }
}