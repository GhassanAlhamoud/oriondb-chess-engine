package com.oriondb.query;

import com.oriondb.model.Game;
import com.oriondb.index.IndexManager;
import com.oriondb.core.DatabaseReader;

import java.io.IOException;
import java.util.*;

/**
 * Fluent API for building and executing database queries.
 * Supports chaining multiple search criteria.
 */
public class SearchBuilder {
    private final IndexManager indexManager;
    private final DatabaseReader reader;
    
    private String playerName;
    private String eventName;
    private String ecoCode;
    private String result;
    private Integer minElo;
    private Integer maxElo;
    private String startDate;
    private String endDate;
    
    public SearchBuilder(IndexManager indexManager, DatabaseReader reader) {
        this.indexManager = indexManager;
        this.reader = reader;
    }
    
    /**
     * Filter by player name (matches either White or Black).
     * Case-insensitive.
     */
    public SearchBuilder withPlayer(String playerName) {
        this.playerName = playerName;
        return this;
    }
    
    /**
     * Filter by event name.
     * Case-insensitive.
     */
    public SearchBuilder withEvent(String eventName) {
        this.eventName = eventName;
        return this;
    }
    
    /**
     * Filter by ECO (Encyclopedia of Chess Openings) code.
     */
    public SearchBuilder withEco(String ecoCode) {
        this.ecoCode = ecoCode;
        return this;
    }
    
    /**
     * Filter by game result.
     * Valid values: "1-0", "0-1", "1/2-1/2", "*"
     */
    public SearchBuilder withResult(String result) {
        this.result = result;
        return this;
    }
    
    /**
     * Filter by Elo rating range (inclusive).
     * Matches games where either player's rating is in the range.
     */
    public SearchBuilder withEloRange(int minElo, int maxElo) {
        this.minElo = minElo;
        this.maxElo = maxElo;
        return this;
    }
    
    /**
     * Filter by minimum Elo rating.
     */
    public SearchBuilder withMinElo(int minElo) {
        this.minElo = minElo;
        return this;
    }
    
    /**
     * Filter by maximum Elo rating.
     */
    public SearchBuilder withMaxElo(int maxElo) {
        this.maxElo = maxElo;
        return this;
    }
    
    /**
     * Filter by date range (inclusive).
     * Dates should be in format "YYYY.MM.DD".
     */
    public SearchBuilder withDateRange(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }
    
    /**
     * Execute the query and return matching games.
     * Applies all filters and returns the intersection of results.
     */
    public List<Game> execute() throws IOException {
        // Collect result sets from each filter
        List<Set<Integer>> resultSets = new ArrayList<>();
        
        if (playerName != null) {
            Set<Integer> playerGames = indexManager.findByPlayer(playerName);
            if (playerGames.isEmpty()) {
                return Collections.emptyList(); // Short-circuit if no matches
            }
            resultSets.add(playerGames);
        }
        
        if (eventName != null) {
            Set<Integer> eventGames = indexManager.findByEvent(eventName);
            if (eventGames.isEmpty()) {
                return Collections.emptyList();
            }
            resultSets.add(eventGames);
        }
        
        if (ecoCode != null) {
            Set<Integer> ecoGames = indexManager.findByEco(ecoCode);
            if (ecoGames.isEmpty()) {
                return Collections.emptyList();
            }
            resultSets.add(ecoGames);
        }
        
        if (result != null) {
            Set<Integer> resultGames = indexManager.findByResult(result);
            if (resultGames.isEmpty()) {
                return Collections.emptyList();
            }
            resultSets.add(resultGames);
        }
        
        if (minElo != null || maxElo != null) {
            int min = minElo != null ? minElo : 0;
            int max = maxElo != null ? maxElo : 3000;
            Set<Integer> eloGames = indexManager.findByEloRange(min, max);
            if (eloGames.isEmpty()) {
                return Collections.emptyList();
            }
            resultSets.add(eloGames);
        }
        
        if (startDate != null || endDate != null) {
            String start = startDate != null ? startDate : "0000.00.00";
            String end = endDate != null ? endDate : "9999.99.99";
            Set<Integer> dateGames = indexManager.findByDateRange(start, end);
            if (dateGames.isEmpty()) {
                return Collections.emptyList();
            }
            resultSets.add(dateGames);
        }
        
        // If no filters, return empty list (could return all games, but that's expensive)
        if (resultSets.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Intersect all result sets
        Set<Integer> intersection = new HashSet<>(resultSets.get(0));
        for (int i = 1; i < resultSets.size(); i++) {
            intersection.retainAll(resultSets.get(i));
        }
        
        // Load games from database
        List<Game> games = new ArrayList<>();
        for (Integer gameId : intersection) {
            Long offset = indexManager.getGameOffset(gameId);
            if (offset != null) {
                try {
                    Game game = reader.readGameAt(offset);
                    games.add(game);
                } catch (IOException e) {
                    // Log error and continue
                    System.err.println("Error reading game " + gameId + ": " + e.getMessage());
                }
            }
        }
        
        return games;
    }
    
    /**
     * Execute the query and return the count of matching games.
     * More efficient than execute() when only the count is needed.
     */
    public int count() {
        List<Set<Integer>> resultSets = new ArrayList<>();
        
        if (playerName != null) {
            resultSets.add(indexManager.findByPlayer(playerName));
        }
        if (eventName != null) {
            resultSets.add(indexManager.findByEvent(eventName));
        }
        if (ecoCode != null) {
            resultSets.add(indexManager.findByEco(ecoCode));
        }
        if (result != null) {
            resultSets.add(indexManager.findByResult(result));
        }
        if (minElo != null || maxElo != null) {
            int min = minElo != null ? minElo : 0;
            int max = maxElo != null ? maxElo : 3000;
            resultSets.add(indexManager.findByEloRange(min, max));
        }
        if (startDate != null || endDate != null) {
            String start = startDate != null ? startDate : "0000.00.00";
            String end = endDate != null ? endDate : "9999.99.99";
            resultSets.add(indexManager.findByDateRange(start, end));
        }
        
        if (resultSets.isEmpty()) {
            return 0;
        }
        
        Set<Integer> intersection = new HashSet<>(resultSets.get(0));
        for (int i = 1; i < resultSets.size(); i++) {
            intersection.retainAll(resultSets.get(i));
        }
        
        return intersection.size();
    }
}
