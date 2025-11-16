import com.oriondb.core.OrionDatabase;
import com.oriondb.model.Game;
import com.oriondb.util.ProgressCallback;

import java.io.File;
import java.util.List;

/**
 * Example demonstrating advanced OrionDB features:
 * - Position indexing
 * - Material and structure search
 * - Chess Query Language (CQL)
 * - Comment indexing with Lucene
 */
public class AdvancedFeaturesExample {
    
    public static void main(String[] args) throws Exception {
        // Example 1: Create database with full indexing
        createDatabaseWithFullIndexing();
        
        // Example 2: Use Chess Query Language
        useCQL();
        
        // Example 3: Position-based searches (when integrated)
        // positionBasedSearch();
    }
    
    /**
     * Create a database with position and comment indexing enabled.
     */
    public static void createDatabaseWithFullIndexing() throws Exception {
        System.out.println("=== Creating Database with Full Indexing ===\n");
        
        File pgnFile = new File("examples/sample.pgn");
        File dbFile = new File("examples/advanced_games.oriondb");
        
        // Enable both position and comment indexing
        OrionDatabase.ImportStats stats = OrionDatabase.createFromPgn(
            pgnFile,
            dbFile,
            ProgressCallback.CONSOLE,
            true,  // Enable position indexing
            true   // Enable comment indexing
        );
        
        System.out.println("\n" + stats);
        System.out.println("\nDatabase created with advanced indexing!");
        System.out.println("This enables:");
        System.out.println("  - Position search by FEN");
        System.out.println("  - Material balance queries");
        System.out.println("  - Pawn structure search");
        System.out.println("  - Full-text comment search");
    }
    
    /**
     * Demonstrate Chess Query Language (CQL) usage.
     */
    public static void useCQL() throws Exception {
        System.out.println("\n\n=== Chess Query Language Examples ===\n");
        
        File dbFile = new File("examples/games.oriondb");
        
        if (!dbFile.exists()) {
            System.out.println("Database not found. Run BasicUsageExample first.");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Simple player and result query
            System.out.println("1. Find Kasparov's wins:");
            List<Game> kasparovWins = db.query("player='Kasparov, Garry' AND result='1-0'");
            System.out.println("   Found " + kasparovWins.size() + " games\n");
            
            // Example 2: ECO and Elo range
            System.out.println("2. High-level Sicilian Najdorf games:");
            List<Game> najdorf = db.query("eco='B90' AND elo >= 2700");
            System.out.println("   Found " + najdorf.size() + " games\n");
            
            // Example 3: Date range query
            System.out.println("3. Games from the 1990s:");
            List<Game> nineties = db.query("date >= '1990.01.01' AND date <= '1999.12.31'");
            System.out.println("   Found " + nineties.size() + " games\n");
            
            // Example 4: Complex query with OR
            System.out.println("4. Kasparov OR Karpov games:");
            List<Game> kk = db.query("(player='Kasparov, Garry' OR player='Karpov, Anatoly') AND result='1-0'");
            System.out.println("   Found " + kk.size() + " games\n");
            
            // Example 5: Event-based query
            System.out.println("5. World Championship games:");
            List<Game> wc = db.query("event CONTAINS 'World' AND event CONTAINS 'Championship'");
            System.out.println("   Found " + wc.size() + " games\n");
        }
    }
    
    /**
     * Demonstrate position-based searches (requires position indexing).
     */
    public static void positionBasedSearch() throws Exception {
        System.out.println("\n\n=== Position-Based Search Examples ===\n");
        
        File dbFile = new File("examples/advanced_games.oriondb");
        
        if (!dbFile.exists()) {
            System.out.println("Advanced database not found. Run createDatabaseWithFullIndexing() first.");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Find games reaching a specific position
            System.out.println("1. Find games with a specific position:");
            String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
            List<Game> posGames = db.query("fen='" + fen + "'");
            System.out.println("   Found " + posGames.size() + " games with this position\n");
            
            // Example 2: Find games with specific pawn structure
            System.out.println("2. Find games with IQP (Isolated Queen's Pawn):");
            List<Game> iqpGames = db.query("structure='IQP'");
            System.out.println("   Found " + iqpGames.size() + " games\n");
            
            // Example 3: Find games with specific material balance
            System.out.println("3. Find endgames:");
            // This would require extended CQL support
            System.out.println("   (Material queries coming soon)\n");
        }
    }
    
    /**
     * Demonstrate comment search (requires comment indexing).
     */
    public static void commentSearch() throws Exception {
        System.out.println("\n\n=== Comment Search Examples ===\n");
        
        File dbFile = new File("examples/advanced_games.oriondb");
        
        if (!dbFile.exists()) {
            System.out.println("Advanced database not found.");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Find games with specific commentary
            System.out.println("1. Find games with 'novelty' in comments:");
            List<Game> novelties = db.query("commentary CONTAINS 'novelty'");
            System.out.println("   Found " + novelties.size() + " games\n");
            
            // Example 2: Find brilliant moves
            System.out.println("2. Find games with brilliant moves (!!):");
            List<Game> brilliant = db.query("annotation='!!'");
            System.out.println("   Found " + brilliant.size() + " games\n");
            
            // Example 3: Combined query
            System.out.println("3. Find high-level games with tactical themes:");
            List<Game> tactical = db.query("elo >= 2700 AND commentary CONTAINS 'tactical'");
            System.out.println("   Found " + tactical.size() + " games\n");
        }
    }
}
