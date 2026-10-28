
package main.java.com.example;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConfig {

    public static Connection getConnection() throws Exception {
        Properties config = new Properties();
        config.load(new FileInputStream("config.properties"));

        String url = "jdbc:postgresql://"
                + config.getProperty("db.host") + ":"
                + config.getProperty("db.port") + "/"
                + config.getProperty("db.name");

        return DriverManager.getConnection(
                url,
                config.getProperty("db.username"),
                config.getProperty("db.password")
        );
    }
}
