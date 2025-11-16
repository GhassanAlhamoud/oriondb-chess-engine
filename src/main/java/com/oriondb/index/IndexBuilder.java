package com.oriondb.index;

import com.oriondb.chess.ChessBoard;
import com.oriondb.chess.PawnStructureClassifier;
import com.oriondb.chess.ZobristHasher;
import com.oriondb.model.*;

import java.util.List;

/**
 * Builds all indexes during database import.
 * Coordinates metadata, position, material, and structure indexing.
 */
public class IndexBuilder {
    private final IndexManager metadataIndex;
    private final PositionIndex positionIndex;
    private final MaterialIndex materialIndex;
    private final StructureIndex structureIndex;
    private final CommentIndex commentIndex;
    
    private final boolean enablePositionIndexing;
    private final boolean enableCommentIndexing;
    
    public IndexBuilder(boolean enablePositionIndexing, boolean enableCommentIndexing) {
        this.metadataIndex = new IndexManager();
        this.positionIndex = enablePositionIndexing ? new PositionIndex() : null;
        this.materialIndex = enablePositionIndexing ? new MaterialIndex() : null;
        this.structureIndex = enablePositionIndexing ? new StructureIndex() : null;
        this.commentIndex = enableCommentIndexing ? new CommentIndex() : null;
        
        this.enablePositionIndexing = enablePositionIndexing;
        this.enableCommentIndexing = enableCommentIndexing;
    }
    
    /**
     * Index a game with all its metadata and positions.
     */
    public void indexGame(Game game, long offset) {
        int gameId = game.getId();
        
        // Index metadata
        metadataIndex.indexGame(game, offset);
        
        // Index positions if enabled
        if (enablePositionIndexing && positionIndex != null) {
            indexPositions(game);
        }
        
        // Index comments if enabled
        if (enableCommentIndexing && commentIndex != null) {
            indexComments(game);
        }
    }
    
    /**
     * Index all positions in a game.
     */
    private void indexPositions(Game game) {
        try {
            ChessBoard board = new ChessBoard();
            int gameId = game.getId();
            int plyCount = 0;
            
            // Index starting position
            indexPosition(board.getPosition(), gameId, plyCount++);
            
            // Apply each move and index resulting position
            for (Move move : game.getMoves()) {
                if (board.applyMove(move.getSan())) {
                    indexPosition(board.getPosition(), gameId, plyCount++);
                } else {
                    // Move application failed, log and continue
                    // In production, this would be logged properly
                    break;
                }
            }
        } catch (Exception e) {
            // Position indexing failed for this game, continue with others
        }
    }
    
    /**
     * Index a single position.
     */
    private void indexPosition(Position position, int gameId, int plyCount) {
        String fen = position.toFen();
        GamePosition gamePos = new GamePosition(gameId, plyCount, fen);
        
        // Zobrist hash index
        long hash = ZobristHasher.hash(position);
        positionIndex.addPosition(hash, gamePos);
        
        // Material index
        MaterialSignature material = MaterialSignature.fromPosition(position);
        materialIndex.addPosition(material, gamePos);
        
        // Structure index
        List<PawnStructure> structures = PawnStructureClassifier.classify(position);
        structureIndex.addPosition(structures, gamePos);
    }
    
    /**
     * Index comments from a game.
     */
    private void indexComments(Game game) {
        int gameId = game.getId();
        int moveNumber = 0;
        
        for (Move move : game.getMoves()) {
            String comment = move.getComment();
            if (comment != null && !comment.isEmpty()) {
                // Create a game position for this comment
                // Note: We don't have the FEN here, so we use a placeholder
                GamePosition gamePos = new GamePosition(gameId, moveNumber, "");
                commentIndex.addComment(comment, gamePos);
            }
            moveNumber++;
        }
    }
    
    /**
     * Get the metadata index manager.
     */
    public IndexManager getMetadataIndex() {
        return metadataIndex;
    }
    
    /**
     * Get the position index.
     */
    public PositionIndex getPositionIndex() {
        return positionIndex;
    }
    
    /**
     * Get the material index.
     */
    public MaterialIndex getMaterialIndex() {
        return materialIndex;
    }
    
    /**
     * Get the structure index.
     */
    public StructureIndex getStructureIndex() {
        return structureIndex;
    }
    
    /**
     * Get the comment index.
     */
    public CommentIndex getCommentIndex() {
        return commentIndex;
    }
    
    /**
     * Get statistics about all indexes.
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Index Statistics:\n");
        sb.append("================\n\n");
        
        sb.append("Metadata Index:\n");
        sb.append("  Games indexed: ").append(metadataIndex.getGameCount()).append("\n\n");
        
        if (positionIndex != null) {
            sb.append(positionIndex.getStats()).append("\n\n");
        }
        
        if (materialIndex != null) {
            sb.append(materialIndex.getStats()).append("\n\n");
        }
        
        if (structureIndex != null) {
            sb.append(structureIndex.getStats()).append("\n\n");
        }
        
        if (commentIndex != null) {
            sb.append(commentIndex.getStats()).append("\n");
        }
        
        return sb.toString();
    }
}
