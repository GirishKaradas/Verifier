package com.example.verifier;

public class DataGrade {

    int index;
    String title;
    String value;

    public DataGrade(int index, String title, String value) {
        this.index = index;
        this.title = title;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
