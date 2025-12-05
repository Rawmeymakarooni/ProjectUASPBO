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

                        // Create menu_items table (new schema uses item_id as primary key)
                        stmt.executeUpdate("PRAGMA foreign_keys = OFF");

                        // Check if menu_items exists and inspect columns
                        boolean menuExists = false;
                        try (ResultSet rt = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='menu_items'")) {
                            menuExists = rt.next();
                        }

                        boolean needsMigration = false;
                        if (menuExists) {
                            try (ResultSet cols = stmt.executeQuery("PRAGMA table_info(menu_items)")) {
                                boolean hasItemId = false;
                                boolean hasId = false;
                                while (cols.next()) {
                                    String colName = cols.getString("name");
                                    if ("item_id".equalsIgnoreCase(colName)) hasItemId = true;
                                    if ("id".equalsIgnoreCase(colName)) hasId = true;
                                }
                                if (!hasItemId) {
                                    // If old column 'id' exists, we'll migrate; otherwise create new table.
                                    needsMigration = hasId;
                                }
                            }
                        }

                        if (!menuExists) {
                            // Create fresh menu_items with item_id primary key
                            stmt.executeUpdate("CREATE TABLE menu_items ("
                                    + "item_id INTEGER PRIMARY KEY,"
                                    + "name TEXT NOT NULL,"
                                    + "category TEXT,"
                                    + "price REAL NOT NULL DEFAULT 0,"
                                    + "stock INTEGER NOT NULL DEFAULT 0"
                                    + ")");
                        } else if (needsMigration) {
                            // Migrate old menu_items(id, ...) -> menu_items(item_id, ...)
                            stmt.executeUpdate("CREATE TABLE menu_items_new ("
                                    + "item_id INTEGER PRIMARY KEY,"
                                    + "name TEXT NOT NULL,"
                                    + "category TEXT,"
                                    + "price REAL NOT NULL DEFAULT 0,"
                                    + "stock INTEGER NOT NULL DEFAULT 0"
                                    + ")");

                            // Copy data: preserve old id values into item_id
                            stmt.executeUpdate("INSERT INTO menu_items_new(item_id, name, category, price, stock) SELECT id, name, category, price, stock FROM menu_items");

                            // Rename old and replace
                            stmt.executeUpdate("ALTER TABLE menu_items RENAME TO menu_items_old");
                            stmt.executeUpdate("ALTER TABLE menu_items_new RENAME TO menu_items");
                            // Drop old
                            stmt.executeUpdate("DROP TABLE IF EXISTS menu_items_old");
                        } else {
                            // menu exists and already has item_id or no id to migrate; ensure new column exists
                            try (ResultSet cols = stmt.executeQuery("PRAGMA table_info(menu_items)")) {
                                boolean hasItemId = false;
                                while (cols.next()) {
                                    if ("item_id".equalsIgnoreCase(cols.getString("name"))) hasItemId = true;
                                }
                                if (!hasItemId) {
                                    // If no item_id, but menu exists without id (unlikely), create new table and copy
                                    stmt.executeUpdate("CREATE TABLE menu_items_new ("
                                            + "item_id INTEGER PRIMARY KEY,"
                                            + "name TEXT NOT NULL,"
                                            + "category TEXT,"
                                            + "price REAL NOT NULL DEFAULT 0,"
                                            + "stock INTEGER NOT NULL DEFAULT 0"
                                            + ")");
                                    stmt.executeUpdate("INSERT INTO menu_items_new(name, category, price, stock) SELECT name, category, price, stock FROM menu_items");
                                    stmt.executeUpdate("ALTER TABLE menu_items RENAME TO menu_items_old");
                                    stmt.executeUpdate("ALTER TABLE menu_items_new RENAME TO menu_items");
                                    stmt.executeUpdate("DROP TABLE IF EXISTS menu_items_old");
                                }
                            }
                        }

                        // Drop legacy orders/order_items tables if present — we now use purchase_history only
                        stmt.executeUpdate("DROP TABLE IF EXISTS order_items");
                        stmt.executeUpdate("DROP TABLE IF EXISTS orders");

                        // Re-enable foreign keys (but we may temporarily toggle it during migration)
                        // We'll check if menu_items currently contains AUTOINCREMENT in its DDL;
                        // if so, recreate menu_items without AUTOINCREMENT while preserving data.

                        // Temporarily turn off foreign keys for safe migration
                        stmt.executeUpdate("PRAGMA foreign_keys = OFF");

                        String menuDdl = null;
                        try (ResultSet rd = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='menu_items'")) {
                            if (rd.next()) menuDdl = rd.getString(1);
                        }

                        if (menuDdl != null && menuDdl.toUpperCase().contains("AUTOINCREMENT")) {
                            // Migrate: create new table without AUTOINCREMENT, copy data, swap
                            System.out.println("Detected AUTOINCREMENT on menu_items — migrating to remove it...");
                            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS menu_items_new ("
                                    + "item_id INTEGER PRIMARY KEY,"
                                    + "name TEXT NOT NULL,"
                                    + "category TEXT,"
                                    + "price REAL NOT NULL DEFAULT 0,"
                                    + "stock INTEGER NOT NULL DEFAULT 0"
                                    + ")");

                            // Copy existing data (preserve item_id values)
                            stmt.executeUpdate("INSERT OR IGNORE INTO menu_items_new(item_id, name, category, price, stock) SELECT item_id, name, category, price, stock FROM menu_items");

                            // Swap tables
                            stmt.executeUpdate("ALTER TABLE menu_items RENAME TO menu_items_old");
                            stmt.executeUpdate("ALTER TABLE menu_items_new RENAME TO menu_items");
                            stmt.executeUpdate("DROP TABLE IF EXISTS menu_items_old");

                            // Remove sqlite_sequence entry for menu_items if exists
                            try (ResultSet rseq = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'")) {
                                if (rseq.next()) {
                                    try {
                                        stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='menu_items'");
                                    } catch (SQLException ignore) {
                                        // ignore if sqlite_sequence doesn't exist or permission issue
                                    }
                                }
                            }

                            System.out.println("Migration complete: menu_items recreated without AUTOINCREMENT.");
                        }

                        // Re-enable foreign keys now that structure is consistent
                        stmt.executeUpdate("PRAGMA foreign_keys = ON");

                        // Ensure purchases table exists (single-table history, no autoincrement id)
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS purchases ("
                                + "purchase_id INTEGER NOT NULL,"
                                + "item_id INTEGER,"
                                + "quantity INTEGER NOT NULL DEFAULT 1,"
                                + "total_price REAL NOT NULL DEFAULT 0,"
                                + "modifier TEXT,"
                                + "timestamp TEXT NOT NULL DEFAULT (datetime('now'))"
                                + " , FOREIGN KEY(item_id) REFERENCES menu_items(item_id)"
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
                        try (PreparedStatement psSelect = conn.prepareStatement("SELECT item_id FROM menu_items WHERE name = ?");
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
                        try (ResultSet rs = stmt.executeQuery("SELECT item_id, name, category, price, stock FROM menu_items ORDER BY item_id")) {
                            System.out.println("menu_items in database:");
                            while (rs.next()) {
                                System.out.printf("%d | %s | %s | Rp %,.0f | stock=%d%n",
                                        rs.getInt("item_id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price"), rs.getInt("stock"));
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
