package com.oriondb.index;

import com.oriondb.model.GamePosition;
import com.oriondb.model.MaterialSignature;

import java.io.Serializable;
import java.util.*;

/**
 * Index for material-based searches.
 * Maps material signatures to game positions.
 */
public class MaterialIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map from material signature to game positions
    private final Map<MaterialSignature, List<GamePosition>> signatureToPositions;
    
    // Map from imbalance value to game positions
    private final TreeMap<Integer, List<GamePosition>> imbalanceToPositions;
    
    public MaterialIndex() {
        this.signatureToPositions = new HashMap<>();
        this.imbalanceToPositions = new TreeMap<>();
    }
    
    /**
     * Add a position to the index.
     */
    public void addPosition(MaterialSignature signature, GamePosition position) {
        // Index by exact signature
        signatureToPositions.computeIfAbsent(signature, k -> new ArrayList<>())
                           .add(position);
        
        // Index by imbalance
        int imbalance = signature.getImbalance();
        imbalanceToPositions.computeIfAbsent(imbalance, k -> new ArrayList<>())
                           .add(position);
    }
    
    /**
     * Find positions with exact material signature.
     */
    public List<GamePosition> findBySignature(MaterialSignature signature) {
        List<GamePosition> positions = signatureToPositions.get(signature);
        return positions != null ? new ArrayList<>(positions) : Collections.emptyList();
    }
    
    /**
     * Find positions within an imbalance range.
     * @param minImbalance Minimum material imbalance (inclusive)
     * @param maxImbalance Maximum material imbalance (inclusive)
     */
    public List<GamePosition> findByImbalanceRange(int minImbalance, int maxImbalance) {
        List<GamePosition> result = new ArrayList<>();
        
        for (Map.Entry<Integer, List<GamePosition>> entry : 
             imbalanceToPositions.subMap(minImbalance, true, maxImbalance, true).entrySet()) {
            result.addAll(entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Find endgame positions (few pieces remaining).
     */
    public List<GamePosition> findEndgames() {
        List<GamePosition> result = new ArrayList<>();
        
        for (Map.Entry<MaterialSignature, List<GamePosition>> entry : signatureToPositions.entrySet()) {
            if (entry.getKey().isEndgame()) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get statistics about the index.
     */
    public String getStats() {
        return String.format(
            "Material Index Statistics:\n" +
            "  Unique signatures: %d\n" +
            "  Imbalance range: %d to %d",
            signatureToPositions.size(),
            imbalanceToPositions.isEmpty() ? 0 : imbalanceToPositions.firstKey(),
            imbalanceToPositions.isEmpty() ? 0 : imbalanceToPositions.lastKey()
        );
    }
}
