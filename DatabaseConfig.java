import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads PostgreSQL connection settings.
 *
 * Order of precedence (highest first):
 *   1. Environment variables: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
 *   2. config.properties file in the project folder
 *   3. Built-in defaults (localhost:5432)
 */
public class DatabaseConfig {

    private static final String CONFIG_FILE = "config.properties";

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private DatabaseConfig(Properties props) {
        this.host = get(props, "DB_HOST", "db.host", "localhost");
        this.port = Integer.parseInt(get(props, "DB_PORT", "db.port", "5432"));
        this.database = get(props, "DB_NAME", "db.name", "postgres");
        this.username = get(props, "DB_USER", "db.username", "postgres");
        this.password = get(props, "DB_PASSWORD", "db.password", "");
    }

    public static DatabaseConfig load() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Note: " + CONFIG_FILE + " not found, using environment variables/defaults.");
        }
        return new DatabaseConfig(props);
    }

    private static String get(Properties props, String envName, String propName, String defaultValue) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return props.getProperty(propName, defaultValue).trim();
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
