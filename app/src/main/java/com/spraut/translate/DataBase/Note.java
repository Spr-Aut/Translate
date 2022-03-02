package com.spraut.translate.DataBase;

public class Note {
    private String keyword;
    private String value;
    private String id;

    @Override
    public String toString() {
        return "Note{" +
                "key='" + keyword + '\'' +
                ", value='" + value + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
