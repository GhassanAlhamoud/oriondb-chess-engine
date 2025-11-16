package com.oriondb.chess;

import com.oriondb.model.Position;
import com.oriondb.model.TacticalMotif;

import java.util.*;

/**
 * Detects tactical motifs in chess positions.
 * Implements algorithms for identifying pins, forks, skewers, and other tactical patterns.
 */
public class TacticalMotifDetector {
    
    private static final int[] PIECE_VALUES = {1, 3, 3, 5, 9, 0}; // P, N, B, R, Q, K
    
    /**
     * Detect all tactical motifs in a position.
     * 
     * @param position The position to analyze
     * @return Set of detected motifs
     */
    public static Set<TacticalMotif> detectMotifs(Position position) {
        Set<TacticalMotif> motifs = new HashSet<>();
        
        if (detectPin(position)) {
            motifs.add(TacticalMotif.PIN);
        }
        
        if (detectFork(position)) {
            motifs.add(TacticalMotif.FORK);
        }
        
        if (detectSkewer(position)) {
            motifs.add(TacticalMotif.SKEWER);
        }
        
        if (detectDoubleAttack(position)) {
            motifs.add(TacticalMotif.DOUBLE_ATTACK);
        }
        
        return motifs;
    }
    
    /**
     * Detect pins in a position.
     * A pin occurs when a piece cannot move without exposing a more valuable piece.
     */
    public static boolean detectPin(Position position) {
        int[] board = position.getBoard();
        
        // Check all sliding pieces (bishops, rooks, queens)
        for (int attacker = 0; attacker < 64; attacker++) {
            int attackerPiece = board[attacker];
            if (attackerPiece == Piece.NONE) continue;
            
            int attackerType = Piece.type(attackerPiece);
            if (attackerType != 2 && attackerType != 3 && attackerType != 4) continue; // Not B, R, or Q
            
            // Get directions for this piece
            int[][] directions = getDirections(attackerType);
            
            for (int[] dir : directions) {
                List<Integer> piecesInLine = getPiecesInDirection(board, attacker, dir);
                
                if (piecesInLine.size() >= 2) {
                    int pinnedSquare = piecesInLine.get(0);
                    int targetSquare = piecesInLine.get(1);
                    
                    int pinnedPiece = board[pinnedSquare];
                    int targetPiece = board[targetSquare];
                    
                    // Check if it's a pin
                    if (Piece.isWhite(attackerPiece) != Piece.isWhite(pinnedPiece) &&
                        Piece.isWhite(attackerPiece) != Piece.isWhite(targetPiece) &&
                        getPieceValue(targetPiece) > getPieceValue(pinnedPiece)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Detect forks in a position.
     * A fork occurs when a piece attacks two or more valuable enemy pieces.
     */
    public static boolean detectFork(Position position) {
        int[] board = position.getBoard();
        
        for (int square = 0; square < 64; square++) {
            int piece = board[square];
            if (piece == Piece.NONE) continue;
            
            Set<Integer> attacks = getAttackedSquares(board, square, piece);
            
            int valuableTargets = 0;
            for (int targetSquare : attacks) {
                int target = board[targetSquare];
                if (target != Piece.NONE &&
                    Piece.isWhite(piece) != Piece.isWhite(target) &&
                    getPieceValue(target) >= getPieceValue(piece)) {
                    valuableTargets++;
                }
            }
            
            if (valuableTargets >= 2) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detect skewers in a position.
     * A skewer is like a reverse pin - a valuable piece is attacked and must move,
     * exposing a less valuable piece behind it.
     */
    public static boolean detectSkewer(Position position) {
        int[] board = position.getBoard();
        
        for (int attacker = 0; attacker < 64; attacker++) {
            int attackerPiece = board[attacker];
            if (attackerPiece == Piece.NONE) continue;
            
            int attackerType = Piece.type(attackerPiece);
            if (attackerType != 2 && attackerType != 3 && attackerType != 4) continue; // Not B, R, or Q
            
            int[][] directions = getDirections(attackerType);
            
            for (int[] dir : directions) {
                List<Integer> piecesInLine = getPiecesInDirection(board, attacker, dir);
                
                if (piecesInLine.size() >= 2) {
                    int frontSquare = piecesInLine.get(0);
                    int backSquare = piecesInLine.get(1);
                    
                    int frontPiece = board[frontSquare];
                    int backPiece = board[backSquare];
                    
                    // Check if it's a skewer
                    if (Piece.isWhite(attackerPiece) != Piece.isWhite(frontPiece) &&
                        Piece.isWhite(attackerPiece) != Piece.isWhite(backPiece) &&
                        getPieceValue(frontPiece) > getPieceValue(backPiece) &&
                        getPieceValue(frontPiece) >= 3) { // Front piece must be at least a minor piece
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Detect double attacks in a position.
     * A double attack occurs when two pieces attack the same target.
     */
    public static boolean detectDoubleAttack(Position position) {
        int[] board = position.getBoard();
        Map<Integer, Integer> attackCount = new HashMap<>();
        
        for (int square = 0; square < 64; square++) {
            int piece = board[square];
            if (piece == Piece.NONE) continue;
            
            Set<Integer> attacks = getAttackedSquares(board, square, piece);
            
            for (int targetSquare : attacks) {
                int target = board[targetSquare];
                if (target != Piece.NONE && Piece.isWhite(piece) != Piece.isWhite(target)) {
                    attackCount.put(targetSquare, attackCount.getOrDefault(targetSquare, 0) + 1);
                }
            }
        }
        
        // Check if any square is attacked by 2+ pieces
        for (int count : attackCount.values()) {
            if (count >= 2) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all squares attacked by a piece.
     */
    private static Set<Integer> getAttackedSquares(int[] board, int square, int piece) {
        Set<Integer> attacks = new HashSet<>();
        int type = Piece.type(piece);
        int file = Square.file(square);
        int rank = Square.rank(square);
        
        switch (type) {
            case 0: // Pawn
                int direction = Piece.isWhite(piece) ? 1 : -1;
                if (file > 0) attacks.add(square + direction * 8 - 1);
                if (file < 7) attacks.add(square + direction * 8 + 1);
                break;
                
            case 1: // Knight
                int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
                for (int[] move : knightMoves) {
                    int newFile = file + move[0];
                    int newRank = rank + move[1];
                    if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                        attacks.add(newRank * 8 + newFile);
                    }
                }
                break;
                
            case 2: // Bishop
                addSlidingAttacks(board, square, attacks, new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}});
                break;
                
            case 3: // Rook
                addSlidingAttacks(board, square, attacks, new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}});
                break;
                
            case 4: // Queen
                addSlidingAttacks(board, square, attacks, 
                    new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}});
                break;
                
            case 5: // King
                int[][] kingMoves = {{0,1}, {0,-1}, {1,0}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};
                for (int[] move : kingMoves) {
                    int newFile = file + move[0];
                    int newRank = rank + move[1];
                    if (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                        attacks.add(newRank * 8 + newFile);
                    }
                }
                break;
        }
        
        return attacks;
    }
    
    /**
     * Add sliding attacks (for bishops, rooks, queens).
     */
    private static void addSlidingAttacks(int[] board, int square, Set<Integer> attacks, int[][] directions) {
        int file = Square.file(square);
        int rank = Square.rank(square);
        
        for (int[] dir : directions) {
            int newFile = file + dir[0];
            int newRank = rank + dir[1];
            
            while (newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) {
                int targetSquare = newRank * 8 + newFile;
                attacks.add(targetSquare);
                
                if (board[targetSquare] != Piece.NONE) {
                    break; // Stop at first piece
                }
                
                newFile += dir[0];
                newRank += dir[1];
            }
        }
    }
    
    /**
     * Get pieces in a specific direction from a square.
     */
    private static List<Integer> getPiecesInDirection(int[] board, int start, int[] direction) {
        List<Integer> pieces = new ArrayList<>();
        int file = Square.file(start);
        int rank = Square.rank(start);
        
        file += direction[0];
        rank += direction[1];
        
        while (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
            int square = rank * 8 + file;
            if (board[square] != Piece.NONE) {
                pieces.add(square);
            }
            file += direction[0];
            rank += direction[1];
        }
        
        return pieces;
    }
    
    /**
     * Get directions for a piece type.
     */
    private static int[][] getDirections(int pieceType) {
        switch (pieceType) {
            case 2: // Bishop
                return new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}};
            case 3: // Rook
                return new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}};
            case 4: // Queen
                return new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};
            default:
                return new int[0][];
        }
    }
    
    /**
     * Get the value of a piece.
     */
    private static int getPieceValue(int piece) {
        if (piece == Piece.NONE) return 0;
        return PIECE_VALUES[Piece.type(piece)];
    }
}
