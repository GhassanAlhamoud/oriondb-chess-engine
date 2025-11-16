package com.oriondb.index;

import com.oriondb.model.GamePosition;

import java.io.Serializable;
import java.util.*;

/**
 * Index for position-based searches using Zobrist hashing.
 * Maps Zobrist hashes to game positions.
 */
public class PositionIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map from Zobrist hash to list of game positions
    private final Map<Long, List<GamePosition>> hashToPositions;
    
    // Statistics
    private int totalPositions = 0;
    private int uniquePositions = 0;
    
    public PositionIndex() {
        this.hashToPositions = new HashMap<>();
    }
    
    /**
     * Add a position to the index.
     * 
     * @param zobristHash Zobrist hash of the position
     * @param position Game position information
     */
    public void addPosition(long zobristHash, GamePosition position) {
        List<GamePosition> positions = hashToPositions.computeIfAbsent(
            zobristHash, k -> new ArrayList<>()
        );
        
        if (positions.isEmpty()) {
            uniquePositions++;
        }
        
        positions.add(position);
        totalPositions++;
    }
    
    /**
     * Find all game positions matching a Zobrist hash.
     * 
     * @param zobristHash Zobrist hash to search for
     * @return List of game positions (empty if none found)
     */
    public List<GamePosition> findByHash(long zobristHash) {
        List<GamePosition> positions = hashToPositions.get(zobristHash);
        return positions != null ? new ArrayList<>(positions) : Collections.emptyList();
    }
    
    /**
     * Get the number of unique positions indexed.
     */
    public int getUniquePositionCount() {
        return uniquePositions;
    }
    
    /**
     * Get the total number of position occurrences indexed.
     */
    public int getTotalPositionCount() {
        return totalPositions;
    }
    
    /**
     * Get statistics about the index.
     */
    public String getStats() {
        double avgOccurrences = uniquePositions > 0 ? 
            (double) totalPositions / uniquePositions : 0.0;
        
        return String.format(
            "Position Index Statistics:\n" +
            "  Total positions: %d\n" +
            "  Unique positions: %d\n" +
            "  Average occurrences: %.2f\n" +
            "  Hash collisions: %d",
            totalPositions,
            uniquePositions,
            avgOccurrences,
            countCollisions()
        );
    }
    
    /**
     * Count hash collisions (positions with same hash but different FEN).
     * This should be extremely rare with 64-bit Zobrist hashing.
     */
    private int countCollisions() {
        int collisions = 0;
        for (List<GamePosition> positions : hashToPositions.values()) {
            if (positions.size() > 1) {
                // Check if any positions have different FENs
                Set<String> fens = new HashSet<>();
                for (GamePosition pos : positions) {
                    fens.add(pos.getFen());
                }
                if (fens.size() > 1) {
                    collisions++;
                }
            }
        }
        return collisions;
    }
    
    /**
     * Clear the index.
     */
    public void clear() {
        hashToPositions.clear();
        totalPositions = 0;
        uniquePositions = 0;
    }
}
