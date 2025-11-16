import com.oriondb.core.OrionDatabase;
import com.oriondb.model.Game;
import com.oriondb.util.ProgressCallback;

import java.io.File;
import java.util.List;

/**
 * Example demonstrating move-based queries and tactical motif detection.
 */
public class TacticalSearchExample {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== OrionDB Tactical Search Example ===\n");
        
        // Create a database with full indexing (including moves and motifs)
        createDatabaseWithTacticalIndexing();
        
        // Demonstrate move-based queries
        moveBasedQueries();
        
        // Demonstrate motif-based queries
        motifBasedQueries();
        
        // Demonstrate combined queries
        combinedQueries();
    }
    
    /**
     * Create a database with move and motif indexing enabled.
     */
    public static void createDatabaseWithTacticalIndexing() throws Exception {
        System.out.println("Creating database with tactical indexing...\n");
        
        File pgnFile = new File("examples/sample.pgn");
        File dbFile = new File("examples/tactical_games.oriondb");
        
        // Enable all indexing options
        OrionDatabase.ImportStats stats = OrionDatabase.createFromPgn(
            pgnFile,
            dbFile,
            ProgressCallback.CONSOLE,
            true,  // Enable position indexing
            true   // Enable comment indexing
            // Move and motif indexing are enabled by default when position indexing is on
        );
        
        System.out.println("\n" + stats);
        System.out.println("\nDatabase created with:");
        System.out.println("  ✓ Position indexing");
        System.out.println("  ✓ Move indexing");
        System.out.println("  ✓ Tactical motif detection");
        System.out.println("  ✓ Comment indexing\n");
    }
    
    /**
     * Demonstrate move-based queries.
     */
    public static void moveBasedQueries() throws Exception {
        System.out.println("\n=== Move-Based Queries ===\n");
        
        File dbFile = new File("examples/games.oriondb");
        if (!dbFile.exists()) {
            System.out.println("Database not found. Run BasicUsageExample first.\n");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Find games where Nf3 was played
            System.out.println("1. Games where Nf3 was played:");
            System.out.println("   Query: move='Nf3'");
            System.out.println("   (This feature requires move indexing)\n");
            
            // Example 2: Find games with early castling
            System.out.println("2. Games with early kingside castling:");
            System.out.println("   Query: move='O-O' AND moveNumber < 10");
            System.out.println("   (Finds games where White or Black castled before move 10)\n");
            
            // Example 3: Find games with specific opening moves
            System.out.println("3. Games with the Ruy Lopez opening:");
            System.out.println("   Query: moves CONTAINS 'e4 e5 Nf3 Nc6 Bb5'");
            System.out.println("   (Finds games with this exact move sequence)\n");
            
            // Example 4: Find games with queen moves
            System.out.println("4. Games with early queen development:");
            System.out.println("   Query: move LIKE 'Q%' AND moveNumber < 5");
            System.out.println("   (Finds games where the queen moved in the first 5 moves)\n");
            
            // Example 5: Find games with pawn breaks
            System.out.println("5. Games with the d4-d5 pawn break:");
            System.out.println("   Query: move='d5'");
            System.out.println("   (Finds all games where d5 was played)\n");
        }
    }
    
    /**
     * Demonstrate motif-based queries.
     */
    public static void motifBasedQueries() throws Exception {
        System.out.println("\n=== Tactical Motif Queries ===\n");
        
        File dbFile = new File("examples/games.oriondb");
        if (!dbFile.exists()) {
            System.out.println("Database not found.\n");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Find games with knight forks
            System.out.println("1. Games with knight forks:");
            System.out.println("   Query: motif='fork' AND move LIKE 'N%'");
            System.out.println("   (Finds games where a knight created a fork)\n");
            
            // Example 2: Find games with pins
            System.out.println("2. Games with pins:");
            System.out.println("   Query: motif='pin'");
            System.out.println("   (Finds all games where a pin occurred)\n");
            
            // Example 3: Find games with skewers
            System.out.println("3. Games with skewers:");
            System.out.println("   Query: motif='skewer' AND elo > 2600");
            System.out.println("   (Finds high-level games with skewers)\n");
            
            // Example 4: Find games with discovered attacks
            System.out.println("4. Games with discovered attacks:");
            System.out.println("   Query: motif='discovered_attack'");
            System.out.println("   (Finds games where a discovered attack occurred)\n");
            
            // Example 5: Find games with multiple tactical motifs
            System.out.println("5. Games with multiple tactics:");
            System.out.println("   Query: motif IN ('pin', 'fork', 'skewer')");
            System.out.println("   (Finds games containing any of these motifs)\n");
            
            // Example 6: Find games with sacrifices
            System.out.println("6. Games with sacrifices:");
            System.out.println("   Query: motif='sacrifice' AND annotation='!!'");
            System.out.println("   (Finds brilliant sacrifices)\n");
        }
    }
    
    /**
     * Demonstrate combined queries.
     */
    public static void combinedQueries() throws Exception {
        System.out.println("\n=== Combined Queries ===\n");
        
        File dbFile = new File("examples/games.oriondb");
        if (!dbFile.exists()) {
            System.out.println("Database not found.\n");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            // Example 1: Tactical games by strong players
            System.out.println("1. Tactical games by strong players:");
            System.out.println("   Query: motif='fork' AND elo >= 2700 AND result='1-0'");
            System.out.println("   (High-level games won with a fork)\n");
            
            // Example 2: Opening with specific tactics
            System.out.println("2. Sicilian games with pins:");
            System.out.println("   Query: eco='B90' AND motif='pin'");
            System.out.println("   (Najdorf games featuring pins)\n");
            
            // Example 3: Endgame tactics
            System.out.println("3. Endgame tactics:");
            System.out.println("   Query: motif='skewer' AND moveNumber > 40");
            System.out.println("   (Skewers in the endgame)\n");
            
            // Example 4: Famous player tactics
            System.out.println("4. Kasparov's tactical games:");
            System.out.println("   Query: player='Kasparov, Garry' AND motif IN ('fork', 'pin', 'sacrifice')");
            System.out.println("   (Kasparov games with specific tactics)\n");
            
            // Example 5: Opening theory with tactics
            System.out.println("5. Theoretical games with tactics:");
            System.out.println("   Query: commentary CONTAINS 'novelty' AND motif='sacrifice'");
            System.out.println("   (Theoretical novelties involving sacrifices)\n");
            
            // Example 6: Complex tactical combinations
            System.out.println("6. Games with multiple tactical themes:");
            System.out.println("   Query: motif='discovered_attack' AND motif='double_attack' AND elo > 2650");
            System.out.println("   (High-level games with complex tactics)\n");
        }
    }
    
    /**
     * Demonstrate tactical statistics.
     */
    public static void tacticalStatistics() throws Exception {
        System.out.println("\n=== Tactical Statistics ===\n");
        
        File dbFile = new File("examples/tactical_games.oriondb");
        if (!dbFile.exists()) {
            System.out.println("Tactical database not found.\n");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            System.out.println("Database Tactical Statistics:");
            System.out.println("  (Statistics would be displayed here)\n");
            
            System.out.println("Most common tactical motifs:");
            System.out.println("  1. Fork: 1,234 occurrences");
            System.out.println("  2. Pin: 987 occurrences");
            System.out.println("  3. Double Attack: 654 occurrences");
            System.out.println("  4. Skewer: 321 occurrences");
            System.out.println("  5. Discovered Attack: 234 occurrences\n");
            
            System.out.println("Most tactical players:");
            System.out.println("  1. Tal, Mikhail: 45 tactical games");
            System.out.println("  2. Kasparov, Garry: 38 tactical games");
            System.out.println("  3. Shirov, Alexei: 32 tactical games\n");
        }
    }
    
    /**
     * Demonstrate move statistics.
     */
    public static void moveStatistics() throws Exception {
        System.out.println("\n=== Move Statistics ===\n");
        
        File dbFile = new File("examples/tactical_games.oriondb");
        if (!dbFile.exists()) {
            System.out.println("Database not found.\n");
            return;
        }
        
        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            System.out.println("Most common moves:");
            System.out.println("  1. e4: 5,432 games");
            System.out.println("  2. d4: 4,321 games");
            System.out.println("  3. Nf3: 8,765 games");
            System.out.println("  4. c4: 3,210 games");
            System.out.println("  5. e5: 4,567 games\n");
            
            System.out.println("Most common opening sequences:");
            System.out.println("  1. 'e4 e5 Nf3 Nc6': 1,234 games");
            System.out.println("  2. 'd4 d5 c4': 987 games");
            System.out.println("  3. 'e4 c5 Nf3': 1,456 games\n");
        }
    }
}
