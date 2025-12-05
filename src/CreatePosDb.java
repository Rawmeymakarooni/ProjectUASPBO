import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Small utility to create an empty pos.db file in the project root using pure Java.
 * Usage (from project root):
 *   javac -d out src\CreatePosDb.java
 *   java -cp out CreatePosDb
 */
public class CreatePosDb {
    public static void main(String[] args) {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path dbPath = projectRoot.resolve("pos.db");

            if (Files.exists(dbPath)) {
                long size = Files.size(dbPath);
                System.out.println("pos.db already exists at: " + dbPath.toAbsolutePath());
                System.out.println("Size: " + size + " bytes");
            } else {
                Files.createFile(dbPath);
                System.out.println("Created empty pos.db at: " + dbPath.toAbsolutePath());
                System.out.println("Size: " + Files.size(dbPath) + " bytes");
            }
        } catch (IOException e) {
            System.err.println("Failed to create pos.db: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

