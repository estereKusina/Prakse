package com.example;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebReader {
  public static List<Files> getDownloadableFiles(String url)
            throws Exception {

        List<Files> files = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString() );

        if (response.statusCode() != 200) {
            throw new Exception("Neizdevās ielādēt lapu!");
        }

        Document document = Jsoup.parse(response.body(), url);

        Elements records = document.select("li.resource-item");

        for (Element record : records) {

            Element downloadButton =record.selectFirst("a[href*='/download/']");

            if (downloadButton != null) {

                String downloadUrl = downloadButton.absUrl("href");

                Element title = record.selectFirst(".heading a");

                String name = title != null? title.text(): record.text();

                name = name.replaceFirst("^\\s*\\d+\\.\\s*", "");

                files.add(new Files(name, downloadUrl));
            }
        }

        return files;
    }   
}
