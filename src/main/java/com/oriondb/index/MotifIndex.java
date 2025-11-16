package com.oriondb.index;

import com.oriondb.model.GamePosition;
import com.oriondb.model.TacticalMotif;

import java.io.Serializable;
import java.util.*;

/**
 * Indexes tactical motifs for fast motif-based queries.
 */
public class MotifIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map from motif type to set of game positions where it occurred
    private final Map<TacticalMotif, Set<GamePosition>> motifToPositions;
    
    // Map from game ID to map of move numbers to motifs at that position
    private final Map<Integer, Map<Integer, Set<TacticalMotif>>> gameToMotifs;
    
    // Statistics
    private int totalMotifs = 0;
    private final Map<TacticalMotif, Integer> motifCounts;
    
    public MotifIndex() {
        this.motifToPositions = new EnumMap<>(TacticalMotif.class);
        this.gameToMotifs = new HashMap<>();
        this.motifCounts = new EnumMap<>(TacticalMotif.class);
        
        // Initialize counts
        for (TacticalMotif motif : TacticalMotif.values()) {
            motifCounts.put(motif, 0);
        }
    }
    
    /**
     * Add a motif to the index.
     * 
     * @param motif The tactical motif
     * @param gameId Game ID
     * @param moveNumber Move number (ply)
     * @param fen FEN string of the position
     */
    public void addMotif(TacticalMotif motif, int gameId, int moveNumber, String fen) {
        GamePosition gamePos = new GamePosition(gameId, moveNumber, fen);
        
        // Add to motif-to-positions map
        motifToPositions.computeIfAbsent(motif, k -> new HashSet<>()).add(gamePos);
        
        // Add to game-to-motifs map
        gameToMotifs.computeIfAbsent(gameId, k -> new HashMap<>())
                    .computeIfAbsent(moveNumber, k -> new HashSet<>())
                    .add(motif);
        
        // Update statistics
        totalMotifs++;
        motifCounts.put(motif, motifCounts.get(motif) + 1);
    }
    
    /**
     * Find all positions where a specific motif occurred.
     * 
     * @param motif The tactical motif to search for
     * @return Set of game positions
     */
    public Set<GamePosition> findMotif(TacticalMotif motif) {
        Set<GamePosition> positions = motifToPositions.get(motif);
        return positions != null ? new HashSet<>(positions) : new HashSet<>();
    }
    
    /**
     * Find games containing a specific motif.
     * 
     * @param motif The tactical motif
     * @return Set of game IDs
     */
    public Set<Integer> findGamesWithMotif(TacticalMotif motif) {
        Set<Integer> games = new HashSet<>();
        Set<GamePosition> positions = findMotif(motif);
        
        for (GamePosition pos : positions) {
            games.add(pos.getGameId());
        }
        
        return games;
    }
    
    /**
     * Find games containing all of the specified motifs.
     * 
     * @param motifs Set of tactical motifs
     * @return Set of game IDs that contain all motifs
     */
    public Set<Integer> findGamesWithAllMotifs(Set<TacticalMotif> motifs) {
        if (motifs.isEmpty()) {
            return new HashSet<>();
        }
        
        Iterator<TacticalMotif> iterator = motifs.iterator();
        Set<Integer> result = findGamesWithMotif(iterator.next());
        
        while (iterator.hasNext()) {
            result.retainAll(findGamesWithMotif(iterator.next()));
        }
        
        return result;
    }
    
    /**
     * Find games containing any of the specified motifs.
     * 
     * @param motifs Set of tactical motifs
     * @return Set of game IDs that contain at least one motif
     */
    public Set<Integer> findGamesWithAnyMotif(Set<TacticalMotif> motifs) {
        Set<Integer> result = new HashSet<>();
        
        for (TacticalMotif motif : motifs) {
            result.addAll(findGamesWithMotif(motif));
        }
        
        return result;
    }
    
    /**
     * Get all motifs that occurred in a specific game.
     * 
     * @param gameId Game ID
     * @return Map from move number to set of motifs at that position
     */
    public Map<Integer, Set<TacticalMotif>> getGameMotifs(int gameId) {
        Map<Integer, Set<TacticalMotif>> motifs = gameToMotifs.get(gameId);
        return motifs != null ? new HashMap<>(motifs) : new HashMap<>();
    }
    
    /**
     * Get motifs at a specific position in a game.
     * 
     * @param gameId Game ID
     * @param moveNumber Move number
     * @return Set of motifs at that position
     */
    public Set<TacticalMotif> getMotifsAtPosition(int gameId, int moveNumber) {
        Map<Integer, Set<TacticalMotif>> gameMotifs = gameToMotifs.get(gameId);
        if (gameMotifs == null) {
            return new HashSet<>();
        }
        
        Set<TacticalMotif> motifs = gameMotifs.get(moveNumber);
        return motifs != null ? new HashSet<>(motifs) : new HashSet<>();
    }
    
    /**
     * Get the count of a specific motif in the database.
     * 
     * @param motif The tactical motif
     * @return Number of occurrences
     */
    public int getMotifCount(TacticalMotif motif) {
        return motifCounts.getOrDefault(motif, 0);
    }
    
    /**
     * Get statistics for all motifs.
     * 
     * @return Map from motif to count
     */
    public Map<TacticalMotif, Integer> getMotifStatistics() {
        return new EnumMap<>(motifCounts);
    }
    
    /**
     * Get the most common motifs.
     * 
     * @param limit Number of motifs to return
     * @return List of motifs sorted by frequency
     */
    public List<Map.Entry<TacticalMotif, Integer>> getMostCommonMotifs(int limit) {
        return motifCounts.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<TacticalMotif, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get statistics about the motif index.
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Motif Index Statistics:\n");
        sb.append("  Total motifs indexed: ").append(totalMotifs).append("\n");
        sb.append("  Games with motifs: ").append(gameToMotifs.size()).append("\n");
        sb.append("  Motif breakdown:\n");
        
        List<Map.Entry<TacticalMotif, Integer>> sorted = getMostCommonMotifs(TacticalMotif.values().length);
        for (Map.Entry<TacticalMotif, Integer> entry : sorted) {
            sb.append("    ").append(entry.getKey().getDisplayName())
              .append(": ").append(entry.getValue()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Get the number of games indexed.
     */
    public int getGameCount() {
        return gameToMotifs.size();
    }
    
    /**
     * Get the total number of motifs indexed.
     */
    public int getTotalMotifCount() {
        return totalMotifs;
    }
}
