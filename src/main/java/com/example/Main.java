package com.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

            HttpClient client = HttpClient.newHttpClient();

            Pattern xmlPattern = Pattern.compile("(?i).*\\.xml$");

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new Exception("Neizdevās ielādēt lapu!");
            }

            Document document = Jsoup.parse(response.body(), url);

            Elements records = document.select("li.resource-item");

            for (Element record : records) {

                Element downloadButton = record.selectFirst("a[href*='/download/']");

                if (downloadButton != null) {

                    String downloadUrl = downloadButton.absUrl("href");

                    Element title = record.selectFirst(".heading a");

                    String name = title != null
                        ? title.text()
                        : record.text();

                    name = name.replaceFirst("^\\s*\\d+\\.\\s*", "");

                    downloadLinks.add(downloadUrl);
                    fileNames.add(name);
                }
            }

            int max = Math.min(downloadLinks.size(), 10);

            System.out.println();
            System.out.println("PIEEJAMIE FAILI");

            for (int i = 0; i < max; i++) {

                System.out.println();
                System.out.println((i + 1) + ". " + fileNames.get(i));
                System.out.println("Lejupielādes saite: " + downloadLinks.get(i));
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

            String downloadUrl = downloadLinks.get(choice - 1);

            URI fileUri = URI.create(downloadUrl);

            String fileName = Path.of(fileUri.getPath())
                .getFileName()
                .toString();

            Path destination = Path.of(
                System.getProperty("user.home"),
                "Downloads",
                fileName
            );

            Files.createDirectories(destination.getParent());

            HttpRequest downloadRequest = HttpRequest.newBuilder().uri(fileUri).GET().build();

            HttpResponse<InputStream> downloadResponse = client.send(downloadRequest,HttpResponse.BodyHandlers.ofInputStream() );

            if (downloadResponse.statusCode() != 200) {
                downloadResponse.body().close();
                throw new Exception("Failu neizdevās lejupielādēt!");
            }

            try (InputStream input = downloadResponse.body()) {
                Files.copy( input, destination,StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println();
            System.out.println("LEJUPIELĀDE PABEIGTA");
            System.out.println("Fails: " + fileName);
            System.out.println("Atrašanās vieta: " + destination);

            System.out.println();
            System.out.println("ATRAHIVĒ ZIP ARHĪVU...");

            List<Path> xmlFiles = new ArrayList<>();

            Path extractFolder = destination.getParent().toAbsolutePath().normalize();

            try (ZipInputStream zip = new ZipInputStream(
                    Files.newInputStream(destination))) {

                ZipEntry entry;

                while ((entry = zip.getNextEntry()) != null) {

                    Path newFile = extractFolder.resolve(entry.getName()).normalize();

                    if (!newFile.startsWith(extractFolder)) {
                        throw new Exception("Nedrošs faila ceļš!");
                    }

                    if (entry.isDirectory()) {

                        Files.createDirectories(newFile);

                    } else {

                        Files.createDirectories(newFile.getParent());

                        Files.copy( zip, newFile,StandardCopyOption.REPLACE_EXISTING);

                        if (xmlPattern.matcher(newFile.getFileName().toString()).matches()) {
                            xmlFiles.add(newFile);
                        }
                    }

                    zip.closeEntry();
                }
            }

            System.out.println("ZIP arhīvs atarhivēts!");

            System.out.println();
            System.out.println("ATRADU XML FAILUS:");
            System.out.println("Kopā atrasti: " + xmlFiles.size());

            if (xmlFiles.isEmpty()) {

                System.out.println("XML faili nav atrasti!");

            } else {

                for (int i = 0; i < xmlFiles.size(); i++) {

                    Path xmlFile = xmlFiles.get(i);

                    System.out.println();
                    System.out.println((i + 1) + ". "
                        + xmlFile.getFileName());

                    System.out.println("Apakšmape: "
                        + extractFolder.relativize(xmlFile.getParent()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
