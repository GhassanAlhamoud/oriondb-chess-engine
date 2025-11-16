package com.oriondb.index;

import com.oriondb.model.Game;

import java.io.*;
import java.util.*;

/**
 * Manages all indexes for the database.
 * Provides fast lookup for metadata queries.
 */
public class IndexManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Game offset index: Game ID -> byte offset in database file
    private final Map<Integer, Long> gameOffsets;
    
    // Inverted indexes for exact matches
    private final Map<String, Set<Integer>> playerIndex;
    private final Map<String, Set<Integer>> eventIndex;
    private final Map<String, Set<Integer>> ecoIndex;
    private final Map<String, Set<Integer>> resultIndex;
    
    // Range indexes (sorted)
    private final TreeMap<Integer, Set<Integer>> eloIndex;
    private final TreeMap<String, Set<Integer>> dateIndex;
    
    public IndexManager() {
        this.gameOffsets = new HashMap<>();
        this.playerIndex = new HashMap<>();
        this.eventIndex = new HashMap<>();
        this.ecoIndex = new HashMap<>();
        this.resultIndex = new HashMap<>();
        this.eloIndex = new TreeMap<>();
        this.dateIndex = new TreeMap<>();
    }
    
    /**
     * Index a game with its offset in the database file.
     */
    public void indexGame(Game game, long offset) {
        int gameId = game.getId();
        
        // Store offset
        gameOffsets.put(gameId, offset);
        
        // Index players
        String white = normalizePlayerName(game.getWhite());
        String black = normalizePlayerName(game.getBlack());
        addToIndex(playerIndex, white, gameId);
        addToIndex(playerIndex, black, gameId);
        
        // Index event
        String event = game.getEvent();
        if (event != null && !event.equals("?")) {
            addToIndex(eventIndex, event.toLowerCase(), gameId);
        }
        
        // Index ECO
        String eco = game.getEco();
        if (eco != null && !eco.isEmpty()) {
            addToIndex(ecoIndex, eco.toUpperCase(), gameId);
        }
        
        // Index result
        String result = game.getResult();
        addToIndex(resultIndex, result, gameId);
        
        // Index Elo ratings
        Integer whiteElo = game.getWhiteElo();
        Integer blackElo = game.getBlackElo();
        if (whiteElo != null) {
            addToIndex(eloIndex, whiteElo, gameId);
        }
        if (blackElo != null) {
            addToIndex(eloIndex, blackElo, gameId);
        }
        
        // Index date
        String date = game.getDate();
        if (date != null && !date.equals("????.??.??")) {
            addToIndex(dateIndex, date, gameId);
        }
    }
    
    /**
     * Normalize player name for consistent indexing.
     * Converts to lowercase and handles "LastName, FirstName" format.
     */
    private String normalizePlayerName(String name) {
        if (name == null || name.equals("?")) {
            return "";
        }
        return name.toLowerCase().trim();
    }
    
    /**
     * Add a game ID to an inverted index.
     */
    private <K> void addToIndex(Map<K, Set<Integer>> index, K key, int gameId) {
        index.computeIfAbsent(key, k -> new HashSet<>()).add(gameId);
    }
    
    /**
     * Get game offset in database file.
     */
    public Long getGameOffset(int gameId) {
        return gameOffsets.get(gameId);
    }
    
    /**
     * Find games by player name (case-insensitive).
     */
    public Set<Integer> findByPlayer(String playerName) {
        String normalized = normalizePlayerName(playerName);
        return playerIndex.getOrDefault(normalized, Collections.emptySet());
    }
    
    /**
     * Find games by event name (case-insensitive).
     */
    public Set<Integer> findByEvent(String eventName) {
        return eventIndex.getOrDefault(eventName.toLowerCase(), Collections.emptySet());
    }
    
    /**
     * Find games by ECO code.
     */
    public Set<Integer> findByEco(String eco) {
        return ecoIndex.getOrDefault(eco.toUpperCase(), Collections.emptySet());
    }
    
    /**
     * Find games by result.
     */
    public Set<Integer> findByResult(String result) {
        return resultIndex.getOrDefault(result, Collections.emptySet());
    }
    
    /**
     * Find games within an Elo range (inclusive).
     */
    public Set<Integer> findByEloRange(Integer minElo, Integer maxElo) {
        Set<Integer> result = new HashSet<>();
        
        for (Map.Entry<Integer, Set<Integer>> entry : eloIndex.subMap(minElo, true, maxElo, true).entrySet()) {
            result.addAll(entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Find games within a date range (inclusive).
     * Dates should be in format "YYYY.MM.DD".
     */
    public Set<Integer> findByDateRange(String startDate, String endDate) {
        Set<Integer> result = new HashSet<>();
        
        for (Map.Entry<String, Set<Integer>> entry : dateIndex.subMap(startDate, true, endDate, true).entrySet()) {
            result.addAll(entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Get total number of indexed games.
     */
    public int getGameCount() {
        return gameOffsets.size();
    }
    
    /**
     * Get statistics about the indexes.
     */
    public String getStats() {
        return String.format(
            "Index Statistics:\n" +
            "  Total games: %d\n" +
            "  Unique players: %d\n" +
            "  Unique events: %d\n" +
            "  Unique ECO codes: %d\n" +
            "  Date range: %s to %s\n" +
            "  Elo range: %d to %d",
            gameOffsets.size(),
            playerIndex.size(),
            eventIndex.size(),
            ecoIndex.size(),
            dateIndex.isEmpty() ? "N/A" : dateIndex.firstKey(),
            dateIndex.isEmpty() ? "N/A" : dateIndex.lastKey(),
            eloIndex.isEmpty() ? 0 : eloIndex.firstKey(),
            eloIndex.isEmpty() ? 0 : eloIndex.lastKey()
        );
    }
    
    /**
     * Save indexes to file.
     */
    public void save(File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(this);
        }
    }
    
    /**
     * Load indexes from file.
     */
    public static IndexManager load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (IndexManager) in.readObject();
        }
    }
}
