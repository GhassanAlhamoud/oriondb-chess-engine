package com.oriondb.model;

import java.io.Serializable;

/**
 * Represents a specific position within a game.
 * Used for position-based indexing.
 */
public class GamePosition implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int gameId;
    private final int moveNumber; // Half-move number (ply)
    private final String fen; // Full FEN string
    
    public GamePosition(int gameId, int moveNumber, String fen) {
        this.gameId = gameId;
        this.moveNumber = moveNumber;
        this.fen = fen;
    }
    
    public int getGameId() {
        return gameId;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public String getFen() {
        return fen;
    }
    
    @Override
    public String toString() {
        return String.format("Game #%d, Move %d: %s", gameId, moveNumber, fen);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePosition that = (GamePosition) o;
        return gameId == that.gameId && moveNumber == that.moveNumber;
    }
    
    @Override
    public int hashCode() {
        return 31 * gameId + moveNumber;
    }
}
