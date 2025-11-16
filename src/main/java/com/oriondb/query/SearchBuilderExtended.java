package com.oriondb.query;

import com.oriondb.model.Game;
import com.oriondb.model.GamePosition;
import com.oriondb.model.PawnStructure;
import com.oriondb.index.*;
import com.oriondb.core.DatabaseReader;

import java.io.IOException;
import java.util.*;

/**
 * Extended SearchBuilder with Phase 2 and Phase 3 features.
 * This extends the basic SearchBuilder with position, material, and comment search.
 */
public class SearchBuilderExtended extends SearchBuilder {
    private final PositionIndex positionIndex;
    private final MaterialIndex materialIndex;
    private final StructureIndex structureIndex;
    private final CommentIndex commentIndex;
    
    private String fenQuery;
    private String pawnStructureQuery;
    private String commentaryQuery;
    
    public SearchBuilderExtended(IndexManager indexManager, DatabaseReader reader,
                                PositionIndex positionIndex, MaterialIndex materialIndex,
                                StructureIndex structureIndex, CommentIndex commentIndex) {
        super(indexManager, reader);
        this.positionIndex = positionIndex;
        this.materialIndex = materialIndex;
        this.structureIndex = structureIndex;
        this.commentIndex = commentIndex;
    }
    
    /**
     * Filter by exact position (FEN).
     */
    public SearchBuilderExtended withFen(String fen) {
        this.fenQuery = fen;
        return this;
    }
    
    /**
     * Filter by pawn structure.
     */
    public SearchBuilderExtended withPawnStructure(String structureName) {
        this.pawnStructureQuery = structureName;
        return this;
    }
    
    /**
     * Filter by commentary text.
     */
    public SearchBuilderExtended withCommentary(String text) {
        this.commentaryQuery = text;
        return this;
    }
    
    /**
     * Execute the extended query.
     */
    @Override
    public List<Game> execute() throws IOException {
        // Start with basic metadata filters
        List<Game> games = super.execute();
        
        // Apply Phase 2/3 filters if specified
        if (fenQuery != null || pawnStructureQuery != null || commentaryQuery != null) {
            Set<Integer> matchingGameIds = new HashSet<>();
            
            // FEN filter
            if (fenQuery != null && positionIndex != null) {
                // This is a simplified version - full implementation would:
                // 1. Parse FEN to Position
                // 2. Compute Zobrist hash
                // 3. Look up in position index
                // For now, just return empty to show the API
                return Collections.emptyList();
            }
            
            // Pawn structure filter
            if (pawnStructureQuery != null && structureIndex != null) {
                try {
                    PawnStructure structure = PawnStructure.valueOf(pawnStructureQuery.toUpperCase());
                    List<GamePosition> positions = structureIndex.findByStructure(structure);
                    for (GamePosition pos : positions) {
                        matchingGameIds.add(pos.getGameId());
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid structure name
                    return Collections.emptyList();
                }
            }
            
            // Commentary filter
            if (commentaryQuery != null && commentIndex != null) {
                List<GamePosition> positions = commentIndex.search(commentaryQuery);
                for (GamePosition pos : positions) {
                    matchingGameIds.add(pos.getGameId());
                }
            }
            
            // Filter games by matching IDs
            if (!matchingGameIds.isEmpty()) {
                games.removeIf(game -> !matchingGameIds.contains(game.getId()));
            }
        }
        
        return games;
    }
}
