package com.oriondb.chess;

/**
 * Represents a square on the chess board (0-63).
 * a1=0, b1=1, ..., h1=7, a2=8, ..., h8=63
 */
public class Square {
    public static final int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public static final int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
    public static final int A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23;
    public static final int A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31;
    public static final int A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39;
    public static final int A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47;
    public static final int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
    public static final int A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;
    
    public static final int NONE = -1;
    
    /**
     * Convert algebraic notation to square index.
     * @param algebraic e.g., "e4", "a1"
     * @return square index 0-63
     */
    public static int fromAlgebraic(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            return NONE;
        }
        char file = algebraic.charAt(0);
        char rank = algebraic.charAt(1);
        
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return NONE;
        }
        
        return (rank - '1') * 8 + (file - 'a');
    }
    
    /**
     * Convert square index to algebraic notation.
     * @param square square index 0-63
     * @return algebraic notation e.g., "e4"
     */
    public static String toAlgebraic(int square) {
        if (square < 0 || square > 63) {
            return "-";
        }
        int file = square % 8;
        int rank = square / 8;
        return "" + (char)('a' + file) + (char)('1' + rank);
    }
    
    /**
     * Get file (0-7) from square.
     */
    public static int file(int square) {
        return square % 8;
    }
    
    /**
     * Get rank (0-7) from square.
     */
    public static int rank(int square) {
        return square / 8;
    }
    
    /**
     * Check if square is valid.
     */
    public static boolean isValid(int square) {
        return square >= 0 && square <= 63;
    }
}
