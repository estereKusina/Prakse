package com.example;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DownloadFileService {

    public static Path downloadFile(String downloadUrl)
            throws VzdFileDownloadException, Exception {

        HttpClient client = HttpClient.newHttpClient();

        URI fileUri = URI.create(downloadUrl);

        String fileName = Path.of(fileUri.getPath()).getFileName().toString();

        Path destination = Path.of(System.getProperty("user.home"), "Downloads",fileName);

        Files.createDirectories(destination.getParent());

        HttpRequest downloadRequest = HttpRequest.newBuilder().uri(fileUri).GET().build();

        HttpResponse<InputStream> downloadResponse = client.send(downloadRequest,HttpResponse.BodyHandlers.ofInputStream());

        if (downloadResponse.statusCode() != 200) {
            downloadResponse.body().close();

            throw new VzdFileDownloadException( "Failu neizdevās lejupielādēt!");
        }

        try (InputStream input = downloadResponse.body()) {

            Files.copy(input,destination,StandardCopyOption.REPLACE_EXISTING);
        }

        return destination;
    }
}