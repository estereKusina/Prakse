import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("System started!");

        DatabaseConfig config = DatabaseConfig.load();
        System.out.println("Connecting to " + config.getJdbcUrl() + " as " + config.getUsername() + "...");

        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(), config.getUsername(), config.getPassword());
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT version()")) {

            if (rs.next()) {
                System.out.println("Connected! " + rs.getString(1));
            }

            // Write your Java code below:


        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}
