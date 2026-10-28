package main.java.com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            System.out.println("Connected to the database!");

        } catch (IOException e) {
            System.out.println("Couldnt read config.properties file!");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Couldnt connect to the database!");
            e.printStackTrace();

        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
