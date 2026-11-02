package com.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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
            java.nio.charset.StandardCharsets.UTF_8
        ));

        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder").setLevel(Level.SEVERE);

        try {

            try (Connection connection = DatabaseConfig.getConnection()) {
                System.out.println("Connected to the database!");
            }

            Properties config = new Properties();

            try (FileInputStream input = new FileInputStream("config.properties")) {
                config.load(input);
            }

            String url = config.getProperty("app.url");

            List<String> downloadLinks = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();

            WebDriver driver = new ChromeDriver();

            try {
                driver.get(url);

                List<WebElement> records = driver.findElements(
                    By.xpath("//li[contains(@class, 'resource-item')]")
                );

                for (WebElement record : records) {

                    List<WebElement> links = record.findElements(
                        By.xpath(".//a[contains(@href, '/download/')]")
                    );

                    for (WebElement link : links) {

                        String downloadUrl = link.getAttribute("href");

                        if (downloadUrl != null) {
                            downloadLinks.add(downloadUrl);
                            fileNames.add(record.getText().replaceAll("\\s+", " ").trim());
                        }
                    }
                }

            } finally {
                driver.quit();
            }

            int max = Math.min(downloadLinks.size(), 10);

            System.out.println();
            System.out.println("PIEEJAMIE FAILI");

            for (int i = 0; i < max; i++) {

                String name = fileNames.get(i).replaceAll("\\s+", " ").trim();
                name = name.replaceFirst("^\\d+\\.\\s*", "");
                name = name.replaceAll("\\s*Izpētīt\\s*$", "");

                System.out.printf("%2d. %s%n", i + 1, name);
            }

            if (max == 0) {
                System.out.println("Nav atrasti lejupielādes faili!");
                return;
            }

            Scanner scanner = new Scanner(System.in);

            System.out.println();
            System.out.print("Norādiet kārtas numuru failam, kuru apstrādāt: ");

            int choice = scanner.nextInt();

            if (choice < 1 || choice > max) {
                System.out.println("Nepareizs numurs!");
                return;
            }

            URL fileUrl = new URL(downloadLinks.get(choice - 1));

            String fileName = fileUrl.getPath().substring(
                fileUrl.getPath().lastIndexOf('/') + 1
            );

            Path destination = Path.of(
                System.getProperty("user.home"),
                "Downloads",
                fileName
            );

            try (InputStream input = fileUrl.openStream()) {
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println();
            System.out.println("LEJUPIELĀDE PABEIGTA");
            System.out.println("Fails: " + fileName);
            System.out.println("Atrašanās vieta: " + destination);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}