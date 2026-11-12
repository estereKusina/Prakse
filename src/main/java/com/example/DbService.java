package com.example;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class DbService {

    public void showCategories() throws Exception {

        String sql = "SELECT * FROM categories ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nKATEGORIJAS");

            while (rs.next()) {

                System.out.println(rs.getInt("id") + ". " +rs.getString("name") +" (" + rs.getString("code") + ")" );
            }
        }
    }

    public void showDatabase() throws Exception {

        String sql = "SELECT v.id, v.title, v.download_date, " +"v.file_path, c.name AS category " +
                "FROM vzd_files v " +
                "JOIN categories c ON v.id_category = c.id " +
                "ORDER BY v.id";

        try (Connection con = DatabaseConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nDATUBĀZES SATURS");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("\nID: " + rs.getInt("id"));
                System.out.println("Nosaukums: " + rs.getString("title"));
                System.out.println("Kategorija: " + rs.getString("category"));
                System.out.println("Lejupielādes datums: "+ rs.getTimestamp("download_date"));
                System.out.println("Faila atrašanās vieta: " + rs.getString("file_path"));
            }

            if (!found) {
                System.out.println("Datubāzē vēl nav failu!");
            }
        }
    }

    public void downloadFileFromDatabase(Scanner scanner) throws Exception {

        String sql = "SELECT id, title FROM vzd_files ORDER BY id";

        try (Connection con = DatabaseConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nDATUBĀZĒ PIEEJAMIE FAILI");

            while (rs.next()) {

                System.out.println(rs.getInt("id") + ". " + rs.getString("title"));
            }
        }

        System.out.print("\nIzvēlieties faila ID: ");

        int id = Integer.parseInt(scanner.nextLine());

        String sqlDownload = "SELECT title, content FROM vzd_files WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlDownload)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Path file = Path.of(System.getProperty("user.home"),"Downloads",rs.getString("title"));

                    Files.createDirectories(file.getParent());

                    Files.write(file, rs.getBytes("content"));

                    System.out.println("\nFails lejupielādēts: " + file);

                } else {

                    System.out.println("Fails nav atrasts!");
                }
            }
        }
    }

    public void importXmlFiles(
            List<Path> xmlFiles,
            int categoryId) throws Exception {

        String sql = "INSERT INTO vzd_files " +"(title, content, download_date, file_path, id_category) " 
        +"VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false);

            try {

                for (Path file : xmlFiles) {

                    if (!Files.isRegularFile(file)) {
                        continue;
                    }

                    byte[] content = Files.readAllBytes(file);

                    String title = file.getFileName().toString();
                    String filePath = file.toAbsolutePath().toString();

                    if (title.length() > 50) {
                        throw new Exception("Faila nosaukums ir garāks par 50 simboliem: "+ title);
                    }

                    if (filePath.length() > 100) {
                        throw new Exception( "Faila ceļš ir garāks par 100 simboliem: " + filePath);
                    }

                    ps.setString(1, title);
                    ps.setBytes(2, content);
                    ps.setString(3, filePath);
                    ps.setInt(4, categoryId);

                    ps.executeUpdate();

                    System.out.println("Saglabāts: " + title);
                }

                con.commit();

                System.out.println( "\nXML faili veiksmīgi saglabāti datubāzē!");

            } catch (Exception e) {

                con.rollback();

                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }
}
