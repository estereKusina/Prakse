package main.java.com.example;

import java.sql.Connection;
import java.awt.Desktop;
import java.net.URI;
import java.io.FileInputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try {
            // Connect to the database
            try (Connection connection = DatabaseConfig.getConnection()) {
                System.out.println("Connected to the database!");
            }

            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            String url = config.getProperty("app.url");
            Desktop.getDesktop().browse(URI.create(url));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}