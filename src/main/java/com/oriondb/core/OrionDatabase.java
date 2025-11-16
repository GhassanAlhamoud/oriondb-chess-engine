package com.oriondb.core;

import com.oriondb.model.Game;
import com.oriondb.parser.PgnParser;
import com.oriondb.index.IndexManager;
import com.oriondb.query.SearchBuilder;
import com.oriondb.util.ProgressCallback;

import java.io.*;
import java.util.List;

/**
 * Main entry point for the OrionDB chess database engine.
 * Provides methods to create databases from PGN files and query them.
 */
public class OrionDatabase implements AutoCloseable {
    private final File dbFile;
    private final File indexFile;
    private final DatabaseReader reader;
    private final IndexManager indexManager;
    
    private OrionDatabase(File dbFile, File indexFile) throws IOException, ClassNotFoundException {
        this.dbFile = dbFile;
        this.indexFile = indexFile;
        this.reader = new DatabaseReader(dbFile);
        this.indexManager = IndexManager.load(indexFile);
    }
    
    /**
     * Create a new database from a PGN file.
     * 
     * @param pgnFile Input PGN file
     * @param outputFile Output database file (.oriondb)
     * @param callback Progress callback (use ProgressCallback.NOOP or ProgressCallback.CONSOLE)
     * @return Statistics about the import
     * @throws IOException if file operations fail
     */
    public static ImportStats createFromPgn(File pgnFile, File outputFile, ProgressCallback callback) 
            throws IOException {
        
        long startTime = System.currentTimeMillis();
        
        // Parse PGN file
        callback.onProgress(0, -1, "Parsing PGN file...");
        PgnParser parser = new PgnParser();
        List<Game> games = parser.parseFile(pgnFile);
        
        callback.onProgress(games.size(), games.size(), "Parsed " + games.size() + " games");
        
        // Write to database
        File indexFile = new File(outputFile.getAbsolutePath() + ".idx");
        
        try (DatabaseWriter writer = new DatabaseWriter(outputFile)) {
            int count = 0;
            for (Game game : games) {
                writer.writeGame(game);
                count++;
                
                if (count % 1000 == 0) {
                    callback.onProgress(count, games.size(), "Writing games to database...");
                }
            }
            
            callback.onProgress(count, games.size(), "Writing indexes...");
            
            // Save indexes
            IndexManager indexManager = writer.getIndexManager();
            indexManager.save(indexFile);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            return new ImportStats(
                games.size(),
                parser.getErrorCount(),
                duration,
                pgnFile.length(),
                outputFile.length(),
                indexManager.getStats()
            );
        }
    }
    
    /**
     * Load an existing database.
     * 
     * @param dbFile Database file (.oriondb)
     * @return OrionDatabase instance
     * @throws IOException if file operations fail
     */
    public static OrionDatabase load(File dbFile) throws IOException, ClassNotFoundException {
        File indexFile = new File(dbFile.getAbsolutePath() + ".idx");
        
        if (!dbFile.exists()) {
            throw new FileNotFoundException("Database file not found: " + dbFile);
        }
        if (!indexFile.exists()) {
            throw new FileNotFoundException("Index file not found: " + indexFile);
        }
        
        return new OrionDatabase(dbFile, indexFile);
    }
    
    /**
     * Get a game by its ID.
     * 
     * @param gameId Game ID
     * @return Game object or null if not found
     * @throws IOException if read fails
     */
    public Game getGameById(int gameId) throws IOException {
        Long offset = indexManager.getGameOffset(gameId);
        if (offset == null) {
            return null;
        }
        return reader.readGameAt(offset);
    }
    
    /**
     * Create a new search query builder.
     * 
     * @return SearchBuilder for fluent query construction
     */
    public SearchBuilder search() {
        return new SearchBuilder(indexManager, reader);
    }
    
    /**
     * Execute a Chess Query Language (CQL) query.
     * 
     * Example: "player='Carlsen' AND elo > 2700 AND result='1-0'"
     * 
     * @param cql CQL query string
     * @return List of matching games
     * @throws IOException if database read fails
     */
    public List<Game> query(String cql) throws IOException {
        CQLQuery cqlQuery = new CQLQuery(search());
        return cqlQuery.query(cql);
    }
    
    /**
     * Get database statistics.
     * 
     * @return Statistics string
     */
    public String getStats() {
        return indexManager.getStats();
    }
    
    /**
     * Get the total number of games in the database.
     * 
     * @return Game count
     */
    public int getGameCount() {
        return indexManager.getGameCount();
    }
    
    @Override
    public void close() throws IOException {
        reader.close();
    }
    
    /**
     * Statistics about a database import operation.
     */
    public static class ImportStats {
        private final int gamesImported;
        private final int errors;
        private final long durationMs;
        private final long pgnSizeBytes;
        private final long dbSizeBytes;
        private final String indexStats;
        
        public ImportStats(int gamesImported, int errors, long durationMs, 
                          long pgnSizeBytes, long dbSizeBytes, String indexStats) {
            this.gamesImported = gamesImported;
            this.errors = errors;
            this.durationMs = durationMs;
            this.pgnSizeBytes = pgnSizeBytes;
            this.dbSizeBytes = dbSizeBytes;
            this.indexStats = indexStats;
        }
        
        public int getGamesImported() {
            return gamesImported;
        }
        
        public int getErrors() {
            return errors;
        }
        
        public long getDurationMs() {
            return durationMs;
        }
        
        public double getGamesPerSecond() {
            return gamesImported / (durationMs / 1000.0);
        }
        
        public double getCompressionRatio() {
            return (double) dbSizeBytes / pgnSizeBytes;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Import Statistics:\n" +
                "  Games imported: %d\n" +
                "  Errors: %d\n" +
                "  Duration: %.2f seconds\n" +
                "  Speed: %.0f games/second\n" +
                "  PGN size: %.2f MB\n" +
                "  DB size: %.2f MB\n" +
                "  Compression: %.1f%%\n\n" +
                "%s",
                gamesImported,
                errors,
                durationMs / 1000.0,
                getGamesPerSecond(),
                pgnSizeBytes / (1024.0 * 1024.0),
                dbSizeBytes / (1024.0 * 1024.0),
                getCompressionRatio() * 100,
                indexStats
            );
        }
    }
}
