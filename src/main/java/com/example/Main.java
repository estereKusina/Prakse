package main.java.com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            System.out.println("Veiksmīgi pievienojāmies datu bāzei!");

        } catch (IOException e) {
            System.out.println("Nevar nolasīt config.properties failu!");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Neizdevās pievienoties datu bāzei!");
            e.printStackTrace();

        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
