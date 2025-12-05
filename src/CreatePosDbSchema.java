import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Create a SQLite database file `pos.db` (in project root) and initialize a simple schema.
 * Requires sqlite-jdbc on the classpath (org.xerial:sqlite-jdbc).
 *
 * Run (PowerShell example):
 *  $jar = "sqlite-jdbc-3.36.0.3.jar"
 *  Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar" -OutFile $jar
 *  javac -d out -cp $jar src\CreatePosDbSchema.java
 *  java -cp "out;$jar" CreatePosDbSchema
 */
public class CreatePosDbSchema {
    private static final String DB_URL = "jdbc:sqlite:pos.db"; // relative to working directory

    public static void main(String[] args) {
        try {
            // Try to explicitly load driver to give a nicer error if not present
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC driver not found on classpath.");
                System.err.println("Please download sqlite-jdbc and run with it on the classpath.");
                System.err.println("See README-INIT-DB.md for exact commands.");
                System.exit(2);
            }

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                if (conn != null) {
                    try (Statement stmt = conn.createStatement()) {
                        conn.setAutoCommit(false);

                        // Create menu_items table
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS menu_items ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "name TEXT NOT NULL,"
                                + "category TEXT,"
                                + "price REAL NOT NULL DEFAULT 0,"
                                + "stock INTEGER NOT NULL DEFAULT 0"
                                + ")");

                        // Create orders table
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "timestamp TEXT NOT NULL DEFAULT (datetime('now')),"
                                + "status TEXT NOT NULL DEFAULT 'Pending',"
                                + "payment_method TEXT,"
                                + "payment_amount REAL DEFAULT 0"
                                + ")");

                        // Create order_items table
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "order_id INTEGER NOT NULL,"
                                + "menu_item_id INTEGER NOT NULL,"
                                + "quantity INTEGER NOT NULL DEFAULT 1,"
                                + "subtotal REAL NOT NULL DEFAULT 0,"
                                + "FOREIGN KEY(order_id) REFERENCES orders(id),"
                                + "FOREIGN KEY(menu_item_id) REFERENCES menu_items(id)"
                                + ")");

                        // Create purchase_history table (single table history, no autoincrement id)
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS purchase_history ("
                                + "purchase_id INTEGER NOT NULL,"
                                + "item_id INTEGER,"
                                + "quantity INTEGER NOT NULL DEFAULT 1,"
                                + "total_price REAL NOT NULL DEFAULT 0,"
                                + "modifier TEXT,"
                                + "timestamp TEXT NOT NULL DEFAULT (datetime('now'))"
                                + ")");

                        // Prepare menu items from POSRestaurant.initializeMenu()
                        String[][] menu = new String[][]{
                                {"Nasi Goreng", "Food", "25000", "50"},
                                {"Rendang", "Food", "35000", "30"},
                                {"Ayam Geprek", "Food", "20000", "40"},
                                {"Soto Ayam", "Food", "18000", "35"},
                                {"Mie Goreng", "Food", "22000", "45"},
                                {"Es Teh Manis", "Beverage", "5000", "100"},
                                {"Kopi Hitam", "Beverage", "8000", "80"},
                                {"Jus Alpukat", "Beverage", "15000", "40"},
                                {"Teh Hangat", "Beverage", "5000", "100"},
                                {"Es Krim", "Dessert", "12000", "50"},
                                {"Pudding", "Dessert", "10000", "40"},
                                {"Pisang Goreng", "Dessert", "8000", "60"}
                        };

                        // Insert menu items if they do not exist
                        try (PreparedStatement psSelect = conn.prepareStatement("SELECT id FROM menu_items WHERE name = ?");
                             PreparedStatement psInsert = conn.prepareStatement("INSERT INTO menu_items(name, category, price, stock) VALUES(?,?,?,?)")) {
                            for (String[] row : menu) {
                                String name = row[0];
                                String category = row[1];
                                double price = Double.parseDouble(row[2]);
                                int stock = Integer.parseInt(row[3]);

                                psSelect.setString(1, name);
                                try (ResultSet rs = psSelect.executeQuery()) {
                                    if (!rs.next()) {
                                        psInsert.setString(1, name);
                                        psInsert.setString(2, category);
                                        psInsert.setDouble(3, price);
                                        psInsert.setInt(4, stock);
                                        psInsert.executeUpdate();
                                    }
                                }
                            }
                        }

                        conn.commit();

                        // Print inserted menu items for verification and show tables including purchase_history
                        try (ResultSet rs = stmt.executeQuery("SELECT id, name, category, price, stock FROM menu_items ORDER BY id")) {
                            System.out.println("menu_items in database:");
                            while (rs.next()) {
                                System.out.printf("%d | %s | %s | Rp %,.0f | stock=%d%n",
                                        rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price"), rs.getInt("stock"));
                            }
                        }

                        System.out.println("\nTables in pos.db:");
                        try (ResultSet rt = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
                            while (rt.next()) {
                                String t = rt.getString("name");
                                System.out.println("- " + t);
                            }
                        }

                        System.out.println("Initialized pos.db and created tables: menu_items, orders, order_items (menu items inserted if absent)");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error while creating schema: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
