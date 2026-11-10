package com.example;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.*;

public class XmlDeserializer {

    public static XmlRecord deserialize(Path file) throws Exception {

        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(file.toFile());

        document.getDocumentElement().normalize();
        return parseElement(document.getDocumentElement());
    }

    
    private static XmlRecord parseElement(Element element) {
        Map<String, String> attributes = new LinkedHashMap<>();
        NamedNodeMap attributeNodes = element.getAttributes();

        for (int i = 0; i < attributeNodes.getLength(); i++) {
           
            Node attribute = attributeNodes.item(i);
            attributes.put(attribute.getNodeName(),attribute.getNodeValue());
        }

        List<XmlRecord> nestedElements = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        NodeList nodes = element.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {

            Node node = nodes.item(i);

            if (node instanceof Element child) {
                nestedElements.add(parseElement(child));

            } else if (node.getNodeType() == Node.TEXT_NODE|| node.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(node.getNodeValue());
            }
        }

        return new XmlRecord( element.getTagName(),text.toString().trim(),attributes, nestedElements );
    }

    public static void printFirst10(XmlRecord root) {

        System.out.println("\nXML ROOT: " + root.getName());

        List<XmlRecord> records = root.getNestedElements();

        int count = Math.min(10, records.size());

        for (int i = 0; i < count; i++) {
            System.out.println("\nRECORD " + (i + 1) + " ");
            printRecord(records.get(i), 0);
        }
    }

    
    private static void printRecord(XmlRecord record, int level) {
        String indent = "  ".repeat(level);

        System.out.println(
                indent + record.getName() + ": " + record.getValue() );

        record.getAttributes().forEach((key, value) ->System.out.println( indent + "@" + key + " = " + value));

        for (XmlRecord nestedElement : record.getNestedElements()) {
             printRecord(nestedElement, level + 1);
        }
    }
}