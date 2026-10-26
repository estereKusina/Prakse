package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
     public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/vzd";
        String user = "prakse2026";
        String password = "prakse2026";

        try (Connection connection =
                 DriverManager.getConnection(url, user, password)) {
            System.out.println("Veiksmīgi pievienojāmies datu bāzei!");
        } catch (SQLException e) {
            System.out.println("Neizdevās pievienoties!");
            e.printStackTrace();
        }
    }
}