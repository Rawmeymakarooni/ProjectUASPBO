import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListTables {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found on classpath.");
            System.exit(2);
        }

        String url = "jdbc:sqlite:pos.db";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            System.out.println("Connected to: " + url);
            System.out.println("Tables in pos.db:");
            try (ResultSet rs = stmt.executeQuery("SELECT name, sql FROM sqlite_master WHERE type='table' ORDER BY name")) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String ddl = rs.getString("sql");
                    System.out.println("- " + name);
                    System.out.println("  DDL: " + ddl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

