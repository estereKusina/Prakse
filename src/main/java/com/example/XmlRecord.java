package com.example;
import java.util.List;
import java.util.Map;

public class XmlRecord {

    private String name;
    private String value;
    private Map<String, String> attributes;
    private List<XmlRecord> nestedElements;

    public XmlRecord(String name, String value, Map<String, String> attributes,List<XmlRecord> nestedElements) {
        this.name = name;
        this.value = value;
        this.attributes = attributes;
        this.nestedElements = nestedElements;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<XmlRecord> getNestedElements() {
        return nestedElements;
    }

    @Override
    public String toString() {
        return name + " = " + value;
    }
}