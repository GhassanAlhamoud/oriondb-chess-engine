package com.oriondb.chess;

/**
 * Represents chess pieces.
 * Uses integer encoding: 0-5 for white pieces, 8-13 for black pieces.
 */
public class Piece {
    // White pieces (0-5)
    public static final int WHITE_PAWN = 0;
    public static final int WHITE_KNIGHT = 1;
    public static final int WHITE_BISHOP = 2;
    public static final int WHITE_ROOK = 3;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_KING = 5;
    
    // Black pieces (8-13)
    public static final int BLACK_PAWN = 8;
    public static final int BLACK_KNIGHT = 9;
    public static final int BLACK_BISHOP = 10;
    public static final int BLACK_ROOK = 11;
    public static final int BLACK_QUEEN = 12;
    public static final int BLACK_KING = 13;
    
    public static final int NONE = -1;
    
    public static final int WHITE = 0;
    public static final int BLACK = 8;
    
    /**
     * Get piece type (0-5) from piece value.
     */
    public static int type(int piece) {
        return piece & 7;
    }
    
    /**
     * Get piece color (0 for white, 8 for black).
     */
    public static int color(int piece) {
        return piece & 8;
    }
    
    /**
     * Check if piece is white.
     */
    public static boolean isWhite(int piece) {
        return (piece & 8) == 0;
    }
    
    /**
     * Check if piece is black.
     */
    public static boolean isBlack(int piece) {
        return (piece & 8) == 8;
    }
    
    /**
     * Get piece character for FEN.
     */
    public static char toFenChar(int piece) {
        char[] chars = {'P', 'N', 'B', 'R', 'Q', 'K'};
        int type = type(piece);
        if (type < 0 || type > 5) return '?';
        char c = chars[type];
        return isBlack(piece) ? Character.toLowerCase(c) : c;
    }
    
    /**
     * Parse piece from FEN character.
     */
    public static int fromFenChar(char c) {
        switch (c) {
            case 'P': return WHITE_PAWN;
            case 'N': return WHITE_KNIGHT;
            case 'B': return WHITE_BISHOP;
            case 'R': return WHITE_ROOK;
            case 'Q': return WHITE_QUEEN;
            case 'K': return WHITE_KING;
            case 'p': return BLACK_PAWN;
            case 'n': return BLACK_KNIGHT;
            case 'b': return BLACK_BISHOP;
            case 'r': return BLACK_ROOK;
            case 'q': return BLACK_QUEEN;
            case 'k': return BLACK_KING;
            default: return NONE;
        }
    }
    
    /**
     * Get piece name.
     */
    public static String getName(int piece) {
        String[] names = {"Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};
        int type = type(piece);
        if (type < 0 || type > 5) return "None";
        String color = isWhite(piece) ? "White" : "Black";
        return color + " " + names[type];
    }
}
