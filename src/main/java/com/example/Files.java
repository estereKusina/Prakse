package com.example;

public class Files {
    private final String name;
    private final String url;

    public Files(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}