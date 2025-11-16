package com.oriondb.index;

import com.oriondb.model.GamePosition;
import com.oriondb.model.PawnStructure;

import java.io.Serializable;
import java.util.*;

/**
 * Index for pawn structure searches.
 * Maps pawn structures to game positions.
 */
public class StructureIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map from pawn structure to game positions
    private final Map<PawnStructure, Set<GamePosition>> structureToPositions;
    
    public StructureIndex() {
        this.structureToPositions = new EnumMap<>(PawnStructure.class);
    }
    
    /**
     * Add a position to the index.
     * A position may have multiple structures.
     */
    public void addPosition(List<PawnStructure> structures, GamePosition position) {
        for (PawnStructure structure : structures) {
            structureToPositions.computeIfAbsent(structure, k -> new HashSet<>())
                               .add(position);
        }
    }
    
    /**
     * Find positions with a specific pawn structure.
     */
    public List<GamePosition> findByStructure(PawnStructure structure) {
        Set<GamePosition> positions = structureToPositions.get(structure);
        return positions != null ? new ArrayList<>(positions) : Collections.emptyList();
    }
    
    /**
     * Get statistics about the index.
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder("Pawn Structure Index Statistics:\n");
        
        for (PawnStructure structure : PawnStructure.values()) {
            Set<GamePosition> positions = structureToPositions.get(structure);
            int count = positions != null ? positions.size() : 0;
            if (count > 0) {
                sb.append(String.format("  %s: %d positions\n", structure.name(), count));
            }
        }
        
        return sb.toString();
    }
}
