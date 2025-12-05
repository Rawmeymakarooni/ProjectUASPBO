# Initialize pos.db (SQLite) with Java

This project includes a small Java utility `CreatePosDbSchema.java` that opens/creates `pos.db` in the project root and initializes a simple schema (tables: `menu_items`, `orders`, `order_items`).

Requirements
- Java JDK (javac, java) installed and available in PATH.
- `sqlite-jdbc` JAR (we'll use org.xerial's driver). This README shows PowerShell commands to download it.

Steps (PowerShell)
1. From the project root (where this README and `pos.db` will reside), download the JDBC driver (example uses version 3.36.0.3):

```powershell
$jar = "sqlite-jdbc-3.36.0.3.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar" -OutFile $jar
```

2. Compile the Java helper:

```powershell
javac -d out -cp $jar src\CreatePosDbSchema.java
```

3. Run the helper (this will create `pos.db` in the current directory and initialize tables):

```powershell
java -cp "out;$jar" CreatePosDbSchema
```

You should see:

```
Initialized pos.db and created tables: menu_items, orders, order_items
```

Notes
- The created `pos.db` will be a valid SQLite database file. You can open it with any SQLite client.
- If you prefer not to download an external JAR, there's also a tiny `CreatePosDb.java` utility in `src/` that simply creates an empty `pos.db` file using pure Java (no JDBC). That file will create an empty placeholder file of size 0 bytes — not a valid SQLite DB. Use `CreatePosDbSchema.java` when you want a real DB with tables.

If you want, I can also add code to pre-populate `menu_items` with the menu items currently defined in `POSRestaurant.initializeMenu()` (names, price, stock). Reply "populate" to have me insert those rows automatically when initializing the DB.
