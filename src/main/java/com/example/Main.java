package com.example;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out),true,java.nio.charset.StandardCharsets.UTF_8));

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

            List<VzdFile> files = WebReader.getDownloadableFiles(url);

            int max = Math.min(files.size(), 10);

            System.out.println();
            System.out.println("PIEEJAMIE FAILI");

            for (int i = 0; i < max; i++) {

                System.out.println();
                System.out.println((i + 1) + ". " + files.get(i).getName());

                System.out.println("Lejupielādes saite: " + files.get(i).getUrl());
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

            String downloadUrl = files.get(choice - 1).getUrl();

            Path downloadedFile = DownloadFileService.downloadFile(downloadUrl);

            System.out.println();
            System.out.println("LEJUPIELĀDE PABEIGTA");

            System.out.println("Fails: " + downloadedFile.getFileName());

            System.out.println("Atrašanās vieta: " + downloadedFile);

            System.out.println();
            System.out.println("ATRAHIVĒ ZIP ARHĪVU...");

            List<Path> xmlFiles = ZipService.extractZipAndFindXml(downloadedFile);

        } catch (VzdFileDownloadException e) {
             System.out.println("Lejupielādes kļūda: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Radās cita kļūda: " + e.getMessage());
            e.printStackTrace();
        }
    }
}