package com.example;

public class VzdFile {
    private final String name;
    private final String url;

    public VzdFile (String name, String url) {
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