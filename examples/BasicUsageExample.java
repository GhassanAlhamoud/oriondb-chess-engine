import com.oriondb.core.OrionDatabase;
import com.oriondb.model.Game;
import com.oriondb.util.ProgressCallback;

import java.io.File;
import java.util.List;

/**
 * Basic usage examples for OrionDB.
 */
public class BasicUsageExample {
    
    public static void main(String[] args) throws Exception {
        // Example 1: Create a database from a PGN file
        createDatabaseExample();
        
        // Example 2: Load and query a database
        queryDatabaseExample();
    }
    
    /**
     * Example: Create a database from a PGN file
     */
    public static void createDatabaseExample() throws Exception {
        File pgnFile = new File("games.pgn");
        File dbFile = new File("games.oriondb");
        
        System.out.println("Creating database from PGN file...");
        
        OrionDatabase.ImportStats stats = OrionDatabase.createFromPgn(
            pgnFile, 
            dbFile, 
            ProgressCallback.CONSOLE
        );
        
        System.out.println("\n" + stats);
    }
    
    /**
     * Example: Load and query a database
     */
    public static void queryDatabaseExample() throws Exception {
        File dbFile = new File("games.oriondb");
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            System.out.println("Database loaded successfully!");
            System.out.println(db.getStats());
            System.out.println();
            
            // Example 1: Find all games by a specific player
            System.out.println("=== Example 1: Find games by player ===");
            List<Game> carlsenGames = db.search()
                .withPlayer("Carlsen, Magnus")
                .execute();
            
            System.out.println("Found " + carlsenGames.size() + " games by Carlsen");
            if (!carlsenGames.isEmpty()) {
                System.out.println("First game: " + carlsenGames.get(0));
            }
            System.out.println();
            
            // Example 2: Find games by opening (ECO code)
            System.out.println("=== Example 2: Find games by opening ===");
            List<Game> sicilianGames = db.search()
                .withEco("B90")  // Sicilian Najdorf
                .execute();
            
            System.out.println("Found " + sicilianGames.size() + " Sicilian Najdorf games");
            System.out.println();
            
            // Example 3: Find high-level games with specific result
            System.out.println("=== Example 3: High-level games ===");
            List<Game> gmGames = db.search()
                .withEloRange(2700, 2900)
                .withResult("1-0")
                .execute();
            
            System.out.println("Found " + gmGames.size() + " games by 2700+ players where White won");
            System.out.println();
            
            // Example 4: Complex query with multiple filters
            System.out.println("=== Example 4: Complex query ===");
            List<Game> complexQuery = db.search()
                .withPlayer("Kasparov, Garry")
                .withEco("E97")  // King's Indian Defense
                .withResult("1-0")
                .withDateRange("1990.01.01", "1999.12.31")
                .execute();
            
            System.out.println("Found " + complexQuery.size() + 
                " games where Kasparov won with King's Indian in the 1990s");
            
            for (Game game : complexQuery) {
                System.out.println("  " + game);
            }
            System.out.println();
            
            // Example 5: Count matching games without loading them
            System.out.println("=== Example 5: Count only ===");
            int count = db.search()
                .withEco("C42")  // Petroff Defense
                .count();
            
            System.out.println("Total Petroff Defense games: " + count);
        }
    }
}
