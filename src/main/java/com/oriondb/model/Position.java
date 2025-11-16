package com.oriondb.model;

import com.oriondb.chess.Piece;
import com.oriondb.chess.Square;

import java.util.Arrays;

/**
 * Represents a chess position with full FEN support.
 * Immutable representation of board state.
 */
public class Position {
    private final int[] board; // 64 squares, each containing a piece or NONE
    private final int sideToMove; // Piece.WHITE or Piece.BLACK
    private final int castlingRights; // Bitmask: 0x1=K, 0x2=Q, 0x4=k, 0x8=q
    private final int enPassantSquare; // Square.NONE or 0-63
    private final int halfMoveClock;
    private final int fullMoveNumber;
    
    public Position(int[] board, int sideToMove, int castlingRights, 
                   int enPassantSquare, int halfMoveClock, int fullMoveNumber) {
        this.board = Arrays.copyOf(board, 64);
        this.sideToMove = sideToMove;
        this.castlingRights = castlingRights;
        this.enPassantSquare = enPassantSquare;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveNumber = fullMoveNumber;
    }
    
    /**
     * Get piece at square.
     */
    public int getPiece(int square) {
        if (!Square.isValid(square)) return Piece.NONE;
        return board[square];
    }
    
    /**
     * Get the board array (copy).
     */
    public int[] getBoard() {
        return Arrays.copyOf(board, 64);
    }
    
    public int getSideToMove() {
        return sideToMove;
    }
    
    public int getCastlingRights() {
        return castlingRights;
    }
    
    public int getEnPassantSquare() {
        return enPassantSquare;
    }
    
    public int getHalfMoveClock() {
        return halfMoveClock;
    }
    
    public int getFullMoveNumber() {
        return fullMoveNumber;
    }
    
    /**
     * Check if white can castle kingside.
     */
    public boolean canCastleWhiteKingside() {
        return (castlingRights & 0x1) != 0;
    }
    
    /**
     * Check if white can castle queenside.
     */
    public boolean canCastleWhiteQueenside() {
        return (castlingRights & 0x2) != 0;
    }
    
    /**
     * Check if black can castle kingside.
     */
    public boolean canCastleBlackKingside() {
        return (castlingRights & 0x4) != 0;
    }
    
    /**
     * Check if black can castle queenside.
     */
    public boolean canCastleBlackQueenside() {
        return (castlingRights & 0x8) != 0;
    }
    
    /**
     * Convert position to FEN string.
     */
    public String toFen() {
        StringBuilder fen = new StringBuilder();
        
        // 1. Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                int piece = board[square];
                
                if (piece == Piece.NONE) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(Piece.toFenChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }
        
        // 2. Side to move
        fen.append(' ');
        fen.append(sideToMove == Piece.WHITE ? 'w' : 'b');
        
        // 3. Castling rights
        fen.append(' ');
        if (castlingRights == 0) {
            fen.append('-');
        } else {
            if (canCastleWhiteKingside()) fen.append('K');
            if (canCastleWhiteQueenside()) fen.append('Q');
            if (canCastleBlackKingside()) fen.append('k');
            if (canCastleBlackQueenside()) fen.append('q');
        }
        
        // 4. En passant square
        fen.append(' ');
        if (enPassantSquare == Square.NONE) {
            fen.append('-');
        } else {
            fen.append(Square.toAlgebraic(enPassantSquare));
        }
        
        // 5. Half move clock
        fen.append(' ');
        fen.append(halfMoveClock);
        
        // 6. Full move number
        fen.append(' ');
        fen.append(fullMoveNumber);
        
        return fen.toString();
    }
    
    /**
     * Parse FEN string to create a Position.
     */
    public static Position fromFen(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid FEN: " + fen);
        }
        
        // Initialize board
        int[] board = new int[64];
        Arrays.fill(board, Piece.NONE);
        
        // 1. Parse piece placement
        String[] ranks = parts[0].split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN piece placement: " + parts[0]);
        }
        
        for (int rank = 0; rank < 8; rank++) {
            String rankStr = ranks[7 - rank]; // FEN starts from rank 8
            int file = 0;
            for (char c : rankStr.toCharArray()) {
                if (Character.isDigit(c)) {
                    file += (c - '0');
                } else {
                    int piece = Piece.fromFenChar(c);
                    if (piece != Piece.NONE && file < 8) {
                        board[rank * 8 + file] = piece;
                        file++;
                    }
                }
            }
        }
        
        // 2. Parse side to move
        int sideToMove = parts[1].equals("w") ? Piece.WHITE : Piece.BLACK;
        
        // 3. Parse castling rights
        int castlingRights = 0;
        if (!parts[2].equals("-")) {
            if (parts[2].contains("K")) castlingRights |= 0x1;
            if (parts[2].contains("Q")) castlingRights |= 0x2;
            if (parts[2].contains("k")) castlingRights |= 0x4;
            if (parts[2].contains("q")) castlingRights |= 0x8;
        }
        
        // 4. Parse en passant square
        int enPassantSquare = parts[3].equals("-") ? Square.NONE : Square.fromAlgebraic(parts[3]);
        
        // 5. Parse half move clock (optional)
        int halfMoveClock = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;
        
        // 6. Parse full move number (optional)
        int fullMoveNumber = parts.length > 5 ? Integer.parseInt(parts[5]) : 1;
        
        return new Position(board, sideToMove, castlingRights, enPassantSquare, 
                          halfMoveClock, fullMoveNumber);
    }
    
    /**
     * Get the starting position.
     */
    public static Position startingPosition() {
        return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }
    
    @Override
    public String toString() {
        return toFen();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return sideToMove == position.sideToMove &&
               castlingRights == position.castlingRights &&
               enPassantSquare == position.enPassantSquare &&
               Arrays.equals(board, position.board);
    }
    
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(board);
        result = 31 * result + sideToMove;
        result = 31 * result + castlingRights;
        result = 31 * result + enPassantSquare;
        return result;
    }
}
