
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

    public static void main(String[] args)throws java.io.UnsupportedEncodingException {
         System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out),true, "UTF-8"));

        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder").setLevel(Level.SEVERE);

        Scanner scanner = new Scanner(System.in);

        DbService dbService = new DbService();

        while (true) {

            try {
                System.out.println();
                System.out.println("KADASTRA DATU SISTĒMA");
                System.out.println("1. Apskatīt datubāzes saturu");
                System.out.println("2. Ielādēt datus no WEB");
                System.out.println("0. Iziet");
                System.out.print("Izvēlieties darbību: ");

                String option = scanner.nextLine();

                switch (option) {

                    case "1":

                        try (Connection connection =DatabaseConfig.getConnection()) {
                             dbService.showDatabase();
                        }

                        continue;

                    case "2":

                        loadDataFromWeb(scanner, dbService);
                        continue;

                    case "0":

                        System.out.println("Programma tiek aizvērta!");
                        return;

                    default:

                        System.out.println("Nepareiza izvēle!");
                        continue;
                }

            } catch (VzdFileDownloadException e) {

                System.out.println("Lejupielādes kļūda: " + e.getMessage());

            } catch (Exception e) {

                System.out.println("Radās cita kļūda: " + e.getMessage());

                e.printStackTrace();
            }
        }
    }

    public static void loadDataFromWeb(
            Scanner scanner, DbService dbService) throws Exception {

        try (Connection connection = DatabaseConfig.getConnection()) {

            System.out.println("Connected to the database!");
        }

        Properties config = new Properties();

        try (FileInputStream input = new FileInputStream("config.properties")) {

            config.load(input);
        }

        String url = config.getProperty("app.url");

        if (url == null || url.isBlank()) {

            System.out.println( "WEB adrese nav norādīta config.properties failā!");
                return;
        }

        List<VzdFile> files =WebReader.getDownloadableFiles(url);

        int max = Math.min(files.size(), 10);

        System.out.println();
        System.out.println("PIEEJAMIE FAILI");

        for (int i = 0; i < max; i++) {

            System.out.println();
            System.out.println((i + 1) + ". " + files.get(i).getName());

            System.out.println("Lejupielādes saite: "+ files.get(i).getUrl());
        }

        if (max == 0) {

            System.out.println("Nav atrasti lejupielādes faili!");
            return;
        }

        System.out.println();
        System.out.print( "Norādiet kārtas numuru failam, kuru apstrādāt: ");

        int choice;

        try {

            choice = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {

            System.out.println("Nepareiza ievade!");
            return;
        }

        if (choice < 1 || choice > max) {

            System.out.println("Nepareizs numurs!");
            return;
        }

        String downloadUrl =files.get(choice - 1).getUrl();

        Path downloadedFile =DownloadFileService.downloadFile(downloadUrl);

        System.out.println();
        System.out.println("LEJUPIELĀDE PABEIGTA");

        System.out.println("Fails: "+ downloadedFile.getFileName());

        System.out.println("Atrašanās vieta: "+ downloadedFile);

        System.out.println();
        System.out.println("ATRAHIVĒ ZIP ARHĪVU...");

        List<Path> xmlFiles = ZipService.extractZipAndFindXml(downloadedFile);

        if (xmlFiles.isEmpty()) {
            System.out.println("XML faili nav atrasti!");
            return;
        }

        System.out.println();
        System.out.println("XML DESERIALIZĀCIJA");

        System.out.println("Atrasti XML faili: "+ xmlFiles.size());

        System.out.print("Vai vēlaties nolasīt pirmo XML failu? (J/N): ");

        String deserializeAnswer = scanner.nextLine();

        if (deserializeAnswer.equalsIgnoreCase("J")) {

            Path xmlFile = xmlFiles.get(0);

            System.out.println();
            System.out.println("Nolasāmais fails: " + xmlFile);

            XmlRecord data =XmlDeserializer.deserialize(xmlFile);

            System.out.println();
            System.out.println("XML fails veiksmīgi deserializēts!");

            XmlDeserializer.printFirst10(data);
        }


        System.out.println();

        System.out.print( "Vai vēlaties ierakstīt XML failus datubāzē? (J/N): ");

        String answer = scanner.nextLine();

        if (answer.equalsIgnoreCase("J")) {

            dbService.showCategories();

            System.out.print("\nIevadiet kategorijas ID: ");

            int categoryId;

            try {

                categoryId = Integer.parseInt(scanner.nextLine());

            } catch (NumberFormatException e) {

                System.out.println("Nepareizs kategorijas ID!");
                return;
            }

            dbService.importXmlFiles(xmlFiles, categoryId);

        } else {

            System.out.println(
                    "XML faili saglabāti datorā. Datubāze nav mainīta.");
        }
    }
}
