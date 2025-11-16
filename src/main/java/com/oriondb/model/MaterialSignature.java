package com.oriondb.model;

import com.oriondb.chess.Piece;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the material balance of a position.
 * Counts pieces for each side.
 */
public class MaterialSignature implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int whiteQueens;
    private final int whiteRooks;
    private final int whiteBishops;
    private final int whiteKnights;
    private final int whitePawns;
    
    private final int blackQueens;
    private final int blackRooks;
    private final int blackBishops;
    private final int blackKnights;
    private final int blackPawns;
    
    public MaterialSignature(int whiteQueens, int whiteRooks, int whiteBishops, 
                            int whiteKnights, int whitePawns,
                            int blackQueens, int blackRooks, int blackBishops,
                            int blackKnights, int blackPawns) {
        this.whiteQueens = whiteQueens;
        this.whiteRooks = whiteRooks;
        this.whiteBishops = whiteBishops;
        this.whiteKnights = whiteKnights;
        this.whitePawns = whitePawns;
        this.blackQueens = blackQueens;
        this.blackRooks = blackRooks;
        this.blackBishops = blackBishops;
        this.blackKnights = blackKnights;
        this.blackPawns = blackPawns;
    }
    
    /**
     * Create material signature from a position.
     */
    public static MaterialSignature fromPosition(Position position) {
        int[] counts = new int[14];
        
        for (int square = 0; square < 64; square++) {
            int piece = position.getPiece(square);
            if (piece != Piece.NONE) {
                counts[piece]++;
            }
        }
        
        return new MaterialSignature(
            counts[Piece.WHITE_QUEEN],
            counts[Piece.WHITE_ROOK],
            counts[Piece.WHITE_BISHOP],
            counts[Piece.WHITE_KNIGHT],
            counts[Piece.WHITE_PAWN],
            counts[Piece.BLACK_QUEEN],
            counts[Piece.BLACK_ROOK],
            counts[Piece.BLACK_BISHOP],
            counts[Piece.BLACK_KNIGHT],
            counts[Piece.BLACK_PAWN]
        );
    }
    
    /**
     * Get material imbalance (positive = white ahead, negative = black ahead).
     * Using standard piece values: Q=9, R=5, B=3, N=3, P=1
     */
    public int getImbalance() {
        int whiteMaterial = whiteQueens * 9 + whiteRooks * 5 + 
                           whiteBishops * 3 + whiteKnights * 3 + whitePawns;
        int blackMaterial = blackQueens * 9 + blackRooks * 5 + 
                           blackBishops * 3 + blackKnights * 3 + blackPawns;
        return whiteMaterial - blackMaterial;
    }
    
    /**
     * Get total piece count (excluding kings).
     */
    public int getTotalPieceCount() {
        return whiteQueens + whiteRooks + whiteBishops + whiteKnights + whitePawns +
               blackQueens + blackRooks + blackBishops + blackKnights + blackPawns;
    }
    
    /**
     * Check if this is an endgame (few pieces remaining).
     */
    public boolean isEndgame() {
        return getTotalPieceCount() <= 10;
    }
    
    /**
     * Convert to string notation (e.g., "Q+R+3P vs R+B+2P").
     */
    @Override
    public String toString() {
        return toNotation(true) + " vs " + toNotation(false);
    }
    
    private String toNotation(boolean white) {
        StringBuilder sb = new StringBuilder();
        int q = white ? whiteQueens : blackQueens;
        int r = white ? whiteRooks : blackRooks;
        int b = white ? whiteBishops : blackBishops;
        int n = white ? whiteKnights : blackKnights;
        int p = white ? whitePawns : blackPawns;
        
        if (q > 0) sb.append(q > 1 ? q + "Q" : "Q").append("+");
        if (r > 0) sb.append(r > 1 ? r + "R" : "R").append("+");
        if (b > 0) sb.append(b > 1 ? b + "B" : "B").append("+");
        if (n > 0) sb.append(n > 1 ? n + "N" : "N").append("+");
        if (p > 0) sb.append(p > 1 ? p + "P" : "P").append("+");
        
        if (sb.length() == 0) return "K";
        if (sb.charAt(sb.length() - 1) == '+') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialSignature that = (MaterialSignature) o;
        return whiteQueens == that.whiteQueens &&
               whiteRooks == that.whiteRooks &&
               whiteBishops == that.whiteBishops &&
               whiteKnights == that.whiteKnights &&
               whitePawns == that.whitePawns &&
               blackQueens == that.blackQueens &&
               blackRooks == that.blackRooks &&
               blackBishops == that.blackBishops &&
               blackKnights == that.blackKnights &&
               blackPawns == that.blackPawns;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(whiteQueens, whiteRooks, whiteBishops, whiteKnights, whitePawns,
                          blackQueens, blackRooks, blackBishops, blackKnights, blackPawns);
    }
}
