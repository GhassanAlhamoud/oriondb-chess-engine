package com.oriondb.chess;

import com.oriondb.model.Position;

import java.util.Random;

/**
 * Zobrist hashing for chess positions.
 * Generates 64-bit hash values that uniquely identify positions.
 */
public class ZobristHasher {
    // Random keys for each piece on each square
    private static final long[][] PIECE_KEYS = new long[14][64];
    
    // Random key for black to move
    private static final long BLACK_TO_MOVE_KEY;
    
    // Random keys for castling rights (4 bits)
    private static final long[] CASTLING_KEYS = new long[16];
    
    // Random keys for en passant file (8 files)
    private static final long[] EN_PASSANT_KEYS = new long[8];
    
    static {
        // Initialize with fixed seed for reproducibility
        Random random = new Random(0x123456789ABCDEFL);
        
        // Generate piece-square keys
        for (int piece = 0; piece < 14; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_KEYS[piece][square] = random.nextLong();
            }
        }
        
        // Generate side to move key
        BLACK_TO_MOVE_KEY = random.nextLong();
        
        // Generate castling keys
        for (int i = 0; i < 16; i++) {
            CASTLING_KEYS[i] = random.nextLong();
        }
        
        // Generate en passant keys
        for (int i = 0; i < 8; i++) {
            EN_PASSANT_KEYS[i] = random.nextLong();
        }
    }
    
    /**
     * Compute Zobrist hash for a position.
     */
    public static long hash(Position position) {
        long hash = 0L;
        
        // Hash pieces
        int[] board = position.getBoard();
        for (int square = 0; square < 64; square++) {
            int piece = board[square];
            if (piece != Piece.NONE) {
                hash ^= PIECE_KEYS[piece][square];
            }
        }
        
        // Hash side to move
        if (position.getSideToMove() == Piece.BLACK) {
            hash ^= BLACK_TO_MOVE_KEY;
        }
        
        // Hash castling rights
        hash ^= CASTLING_KEYS[position.getCastlingRights()];
        
        // Hash en passant
        int epSquare = position.getEnPassantSquare();
        if (epSquare != Square.NONE) {
            int file = Square.file(epSquare);
            hash ^= EN_PASSANT_KEYS[file];
        }
        
        return hash;
    }
    
    /**
     * Get the piece-square key for incremental updates.
     */
    public static long getPieceSquareKey(int piece, int square) {
        if (piece == Piece.NONE || !Square.isValid(square)) {
            return 0L;
        }
        return PIECE_KEYS[piece][square];
    }
    
    /**
     * Get the black to move key.
     */
    public static long getBlackToMoveKey() {
        return BLACK_TO_MOVE_KEY;
    }
    
    /**
     * Get the castling rights key.
     */
    public static long getCastlingKey(int castlingRights) {
        return CASTLING_KEYS[castlingRights & 0xF];
    }
    
    /**
     * Get the en passant key for a file.
     */
    public static long getEnPassantKey(int file) {
        if (file < 0 || file > 7) {
            return 0L;
        }
        return EN_PASSANT_KEYS[file];
    }
}
