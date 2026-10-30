package com.example;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;


public class Main {

    public static void main(String[] args) {

    System.setOut(new java.io.PrintStream(
        new java.io.FileOutputStream(
            java.io.FileDescriptor.out
        ),
        true,
        StandardCharsets.UTF_8
    ));
        // Suppress Selenium CDP version warning
        Logger.getLogger(
            "org.openqa.selenium.devtools.CdpVersionFinder"
        ).setLevel(Level.OFF);

        try {

            try (Connection connection =
                     DatabaseConfig.getConnection()) {

                System.out.println("Connected to the database!");
            }

            Properties config = new Properties();

            try (FileInputStream input =
                     new FileInputStream("config.properties")) {

                config.load(input);
            }

            String url = config.getProperty("app.url");

            WebDriver driver = new ChromeDriver();

            try {

                driver.get(url);

                List<WebElement> records =
                    driver.findElements(
                        By.xpath("//li[contains(@class, 'resource-item')]")
                    );

                System.out.println("\nDati un resursi:\n");

                for (WebElement record : records) {

                    System.out.println(
                        record.getText()
                              .replaceAll("\\s+", " ")
                              .trim()
                    );
                }

            } finally {
                driver.quit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}