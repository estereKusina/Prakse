package com.example;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipService {
       public static List<Path> extractZipAndFindXml(Path zipFile)
            throws Exception {

        List<Path> xmlFiles = new ArrayList<>();

        Pattern xmlPattern = Pattern.compile("(?i).*\\.xml$");

        Path extractFolder = zipFile.getParent().toAbsolutePath().normalize();

        try (ZipInputStream zip = new ZipInputStream(
                Files.newInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                Path newFile = extractFolder.resolve(entry.getName()).normalize();

                if (entry.isDirectory()) {

                    Files.createDirectories(newFile);

                } else {

                    Files.createDirectories(newFile.getParent());

                    Files.copy(zip,newFile,StandardCopyOption.REPLACE_EXISTING );

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
                System.out.println((i + 1) + ". " + xmlFile.getFileName());
                 System.out.println("Apakšmape: " + extractFolder.relativize(xmlFile.getParent()));
            }
        }

        return xmlFiles;
    }
}
