package com.oriondb.chess;

import com.oriondb.model.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a chess board with the ability to apply moves.
 * Maintains position history for the entire game.
 */
public class ChessBoard {
    private Position currentPosition;
    private final List<Position> positionHistory;
    
    /**
     * Create a board from a starting position.
     */
    public ChessBoard(Position startPosition) {
        this.currentPosition = startPosition;
        this.positionHistory = new ArrayList<>();
        this.positionHistory.add(startPosition);
    }
    
    /**
     * Create a board with the standard starting position.
     */
    public ChessBoard() {
        this(Position.startingPosition());
    }
    
    /**
     * Apply a move in Standard Algebraic Notation (SAN).
     * 
     * @param san Move in SAN format (e.g., "e4", "Nf3", "O-O")
     * @return true if move was applied successfully, false otherwise
     */
    public boolean applyMove(String san) {
        try {
            MoveParser.ParsedMove parsed = MoveParser.parse(san, currentPosition.getSideToMove());
            if (parsed == null) {
                return false;
            }
            
            // Handle castling
            if (parsed.isCastleKingside) {
                currentPosition = applyCastling(true);
                if (currentPosition != null) {
                    positionHistory.add(currentPosition);
                    return true;
                }
                return false;
            }
            
            if (parsed.isCastleQueenside) {
                currentPosition = applyCastling(false);
                if (currentPosition != null) {
                    positionHistory.add(currentPosition);
                    return true;
                }
                return false;
            }
            
            // Find source square
            int fromSquare = findSourceSquare(parsed);
            if (fromSquare == Square.NONE) {
                return false; // Could not find source square
            }
            
            // Apply the move
            currentPosition = applyNormalMove(fromSquare, parsed.toSquare, parsed.promotionPiece);
            if (currentPosition != null) {
                positionHistory.add(currentPosition);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            // Move application failed
            return false;
        }
    }
    
    /**
     * Find the source square for a move based on parsed information.
     */
    private int findSourceSquare(MoveParser.ParsedMove parsed) {
        int[] board = currentPosition.getBoard();
        
        // Check all squares for matching piece
        for (int square = 0; square < 64; square++) {
            int piece = board[square];
            
            // Must match piece type
            if (piece != parsed.pieceType) {
                continue;
            }
            
            // Check file disambiguation
            if (parsed.fromFile != -1 && Square.file(square) != parsed.fromFile) {
                continue;
            }
            
            // Check rank disambiguation
            if (parsed.fromRank != -1 && Square.rank(square) != parsed.fromRank) {
                continue;
            }
            
            // Check if this piece can reach the destination
            if (canPieceReach(square, parsed.toSquare, parsed.pieceType)) {
                return square;
            }
        }
        
        return Square.NONE;
    }
    
    /**
     * Check if a piece can reach a destination square (simplified validation).
     */
    private boolean canPieceReach(int from, int to, int pieceType) {
        int[] board = currentPosition.getBoard();
        int fromFile = Square.file(from);
        int fromRank = Square.rank(from);
        int toFile = Square.file(to);
        int toRank = Square.rank(to);
        
        int fileDiff = Math.abs(toFile - fromFile);
        int rankDiff = Math.abs(toRank - fromRank);
        
        int type = Piece.type(pieceType);
        
        switch (type) {
            case 0: // Pawn
                boolean isWhite = Piece.isWhite(pieceType);
                int direction = isWhite ? 1 : -1;
                
                // Forward move
                if (fromFile == toFile && board[to] == Piece.NONE) {
                    if (toRank == fromRank + direction) return true;
                    // Double move from starting position
                    if ((isWhite && fromRank == 1 && toRank == 3) ||
                        (!isWhite && fromRank == 6 && toRank == 4)) {
                        int middleSquare = fromRank + direction * 8 + fromFile;
                        if (board[middleSquare] == Piece.NONE) return true;
                    }
                }
                // Capture
                if (fileDiff == 1 && toRank == fromRank + direction) {
                    if (board[to] != Piece.NONE || to == currentPosition.getEnPassantSquare()) {
                        return true;
                    }
                }
                return false;
                
            case 1: // Knight
                return (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);
                
            case 2: // Bishop
                if (fileDiff != rankDiff) return false;
                return isPathClear(from, to);
                
            case 3: // Rook
                if (fromFile != toFile && fromRank != toRank) return false;
                return isPathClear(from, to);
                
            case 4: // Queen
                if (fromFile != toFile && fromRank != toRank && fileDiff != rankDiff) return false;
                return isPathClear(from, to);
                
            case 5: // King
                return fileDiff <= 1 && rankDiff <= 1;
                
            default:
                return false;
        }
    }
    
    /**
     * Check if path between two squares is clear (for sliding pieces).
     */
    private boolean isPathClear(int from, int to) {
        int[] board = currentPosition.getBoard();
        int fromFile = Square.file(from);
        int fromRank = Square.rank(from);
        int toFile = Square.file(to);
        int toRank = Square.rank(to);
        
        int fileStep = Integer.compare(toFile, fromFile);
        int rankStep = Integer.compare(toRank, fromRank);
        
        int currentFile = fromFile + fileStep;
        int currentRank = fromRank + rankStep;
        
        while (currentFile != toFile || currentRank != toRank) {
            int square = currentRank * 8 + currentFile;
            if (board[square] != Piece.NONE) {
                return false;
            }
            currentFile += fileStep;
            currentRank += rankStep;
        }
        
        return true;
    }
    
    /**
     * Apply a normal move (non-castling).
     */
    private Position applyNormalMove(int from, int to, int promotionPiece) {
        int[] newBoard = currentPosition.getBoard();
        int piece = newBoard[from];
        
        // Move the piece
        newBoard[from] = Piece.NONE;
        newBoard[to] = promotionPiece != Piece.NONE ? promotionPiece : piece;
        
        // Handle en passant capture
        if (Piece.type(piece) == 0 && to == currentPosition.getEnPassantSquare()) {
            int capturedPawnSquare = to + (Piece.isWhite(piece) ? -8 : 8);
            newBoard[capturedPawnSquare] = Piece.NONE;
        }
        
        // Update castling rights
        int newCastlingRights = updateCastlingRights(from, to);
        
        // Update en passant square
        int newEnPassant = Square.NONE;
        if (Piece.type(piece) == 0) {
            int fromRank = Square.rank(from);
            int toRank = Square.rank(to);
            if (Math.abs(toRank - fromRank) == 2) {
                newEnPassant = (from + to) / 2;
            }
        }
        
        // Update clocks
        int newHalfMoveClock = (Piece.type(piece) == 0 || currentPosition.getBoard()[to] != Piece.NONE) 
                              ? 0 : currentPosition.getHalfMoveClock() + 1;
        int newFullMoveNumber = currentPosition.getFullMoveNumber() + 
                               (currentPosition.getSideToMove() == Piece.BLACK ? 1 : 0);
        
        // Switch side to move
        int newSideToMove = currentPosition.getSideToMove() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
        
        return new Position(newBoard, newSideToMove, newCastlingRights, 
                          newEnPassant, newHalfMoveClock, newFullMoveNumber);
    }
    
    /**
     * Apply castling move.
     */
    private Position applyCastling(boolean kingside) {
        int[] newBoard = currentPosition.getBoard();
        boolean isWhite = currentPosition.getSideToMove() == Piece.WHITE;
        
        if (kingside) {
            if (isWhite) {
                newBoard[Square.E1] = Piece.NONE;
                newBoard[Square.H1] = Piece.NONE;
                newBoard[Square.G1] = Piece.WHITE_KING;
                newBoard[Square.F1] = Piece.WHITE_ROOK;
            } else {
                newBoard[Square.E8] = Piece.NONE;
                newBoard[Square.H8] = Piece.NONE;
                newBoard[Square.G8] = Piece.BLACK_KING;
                newBoard[Square.F8] = Piece.BLACK_ROOK;
            }
        } else {
            if (isWhite) {
                newBoard[Square.E1] = Piece.NONE;
                newBoard[Square.A1] = Piece.NONE;
                newBoard[Square.C1] = Piece.WHITE_KING;
                newBoard[Square.D1] = Piece.WHITE_ROOK;
            } else {
                newBoard[Square.E8] = Piece.NONE;
                newBoard[Square.A8] = Piece.NONE;
                newBoard[Square.C8] = Piece.BLACK_KING;
                newBoard[Square.D8] = Piece.BLACK_ROOK;
            }
        }
        
        // Remove castling rights
        int newCastlingRights = currentPosition.getCastlingRights();
        if (isWhite) {
            newCastlingRights &= ~0x3; // Remove white castling rights
        } else {
            newCastlingRights &= ~0xC; // Remove black castling rights
        }
        
        int newSideToMove = isWhite ? Piece.BLACK : Piece.WHITE;
        int newFullMoveNumber = currentPosition.getFullMoveNumber() + (isWhite ? 0 : 1);
        
        return new Position(newBoard, newSideToMove, newCastlingRights, 
                          Square.NONE, currentPosition.getHalfMoveClock() + 1, newFullMoveNumber);
    }
    
    /**
     * Update castling rights based on move.
     */
    private int updateCastlingRights(int from, int to) {
        int rights = currentPosition.getCastlingRights();
        
        // King moves
        if (from == Square.E1) rights &= ~0x3; // White loses both
        if (from == Square.E8) rights &= ~0xC; // Black loses both
        
        // Rook moves or captures
        if (from == Square.H1 || to == Square.H1) rights &= ~0x1; // White kingside
        if (from == Square.A1 || to == Square.A1) rights &= ~0x2; // White queenside
        if (from == Square.H8 || to == Square.H8) rights &= ~0x4; // Black kingside
        if (from == Square.A8 || to == Square.A8) rights &= ~0x8; // Black queenside
        
        return rights;
    }
    
    /**
     * Get the current position.
     */
    public Position getPosition() {
        return currentPosition;
    }
    
    /**
     * Get the position history (all positions in the game).
     */
    public List<Position> getPositionHistory() {
        return new ArrayList<>(positionHistory);
    }
    
    /**
     * Get the number of plies (half-moves) played.
     */
    public int getPlyCount() {
        return positionHistory.size() - 1;
    }
    
    /**
     * Reset to starting position.
     */
    public void reset() {
        currentPosition = Position.startingPosition();
        positionHistory.clear();
        positionHistory.add(currentPosition);
    }
}
