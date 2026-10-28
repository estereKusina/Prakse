package main.java.com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final String CONFIG_FILE = "config.properties";

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws IOException, SQLException {
        Properties config = new Properties();

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
        }

        String host = required(config, "db.host");
        String port = required(config, "db.port");
        String database = required(config, "db.name");
        String username = required(config, "db.username");
        String password = required(config, "db.password");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, username, password);
    }

    private static String required(Properties config, String key) {
        String value = config.getProperty(key);

        if (value == null || value.trim().isEmpty()
                || value.startsWith("YOUR_")) {
            throw new IllegalStateException(
                    "Lūdzu, ievadi īsto vērtību laukam '" + key
                    + "' failā config.properties.");
        }

        return value.trim();
    }
}
