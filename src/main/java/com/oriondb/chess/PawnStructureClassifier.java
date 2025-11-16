package com.oriondb.chess;

import com.oriondb.model.Position;
import com.oriondb.model.PawnStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies pawn structures in chess positions.
 */
public class PawnStructureClassifier {
    
    /**
     * Identify pawn structures in a position.
     * Returns a list of structures found (may be multiple).
     */
    public static List<PawnStructure> classify(Position position) {
        List<PawnStructure> structures = new ArrayList<>();
        
        // Get pawn positions
        boolean[][] whitePawns = new boolean[8][8];
        boolean[][] blackPawns = new boolean[8][8];
        
        for (int square = 0; square < 64; square++) {
            int piece = position.getPiece(square);
            int file = Square.file(square);
            int rank = Square.rank(square);
            
            if (piece == Piece.WHITE_PAWN) {
                whitePawns[file][rank] = true;
            } else if (piece == Piece.BLACK_PAWN) {
                blackPawns[file][rank] = true;
            }
        }
        
        // Check for IQP (Isolated Queen's Pawn)
        if (hasIQP(whitePawns, true) || hasIQP(blackPawns, false)) {
            structures.add(PawnStructure.IQP);
        }
        
        // Check for Maroczy Bind
        if (hasMaroczyBind(whitePawns) || hasMaroczyBind(blackPawns)) {
            structures.add(PawnStructure.MAROCZY_BIND);
        }
        
        // Check for doubled pawns
        if (hasDoubledPawns(whitePawns) || hasDoubledPawns(blackPawns)) {
            structures.add(PawnStructure.DOUBLED_PAWNS);
        }
        
        // Check for passed pawns
        if (hasPassedPawn(whitePawns, blackPawns) || hasPassedPawn(blackPawns, whitePawns)) {
            structures.add(PawnStructure.PASSED_PAWN);
        }
        
        // Check for hanging pawns
        if (hasHangingPawns(whitePawns) || hasHangingPawns(blackPawns)) {
            structures.add(PawnStructure.HANGING_PAWNS);
        }
        
        if (structures.isEmpty()) {
            structures.add(PawnStructure.NONE);
        }
        
        return structures;
    }
    
    /**
     * Check for Isolated Queen's Pawn (IQP).
     * A pawn on the d-file (file 3) with no pawns on c and e files.
     */
    private static boolean hasIQP(boolean[][] pawns, boolean isWhite) {
        int dFile = 3; // d-file
        int cFile = 2; // c-file
        int eFile = 4; // e-file
        
        // Check if there's a pawn on d4 or d5
        int targetRank = isWhite ? 3 : 4; // 4th rank for white, 5th for black
        
        if (pawns[dFile][targetRank]) {
            // Check if c and e files have no pawns
            boolean cFileEmpty = true;
            boolean eFileEmpty = true;
            
            for (int rank = 0; rank < 8; rank++) {
                if (pawns[cFile][rank]) cFileEmpty = false;
                if (pawns[eFile][rank]) eFileEmpty = false;
            }
            
            return cFileEmpty && eFileEmpty;
        }
        
        return false;
    }
    
    /**
     * Check for Maroczy Bind.
     * Pawns on c4 and e4 (or c5 and e5 for black).
     */
    private static boolean hasMaroczyBind(boolean[][] pawns) {
        int cFile = 2;
        int eFile = 4;
        int rank4 = 3; // 4th rank (0-indexed)
        
        return pawns[cFile][rank4] && pawns[eFile][rank4];
    }
    
    /**
     * Check for doubled pawns.
     * Two or more pawns on the same file.
     */
    private static boolean hasDoubledPawns(boolean[][] pawns) {
        for (int file = 0; file < 8; file++) {
            int count = 0;
            for (int rank = 0; rank < 8; rank++) {
                if (pawns[file][rank]) count++;
            }
            if (count >= 2) return true;
        }
        return false;
    }
    
    /**
     * Check for passed pawn.
     * A pawn with no enemy pawns in front of it or on adjacent files.
     */
    private static boolean hasPassedPawn(boolean[][] ourPawns, boolean[][] enemyPawns) {
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                if (ourPawns[file][rank]) {
                    // Check if this pawn is passed
                    boolean isPassed = true;
                    
                    // Check same file ahead
                    for (int r = rank + 1; r < 8; r++) {
                        if (enemyPawns[file][r]) {
                            isPassed = false;
                            break;
                        }
                    }
                    
                    // Check adjacent files ahead
                    if (isPassed && file > 0) {
                        for (int r = rank + 1; r < 8; r++) {
                            if (enemyPawns[file - 1][r]) {
                                isPassed = false;
                                break;
                            }
                        }
                    }
                    
                    if (isPassed && file < 7) {
                        for (int r = rank + 1; r < 8; r++) {
                            if (enemyPawns[file + 1][r]) {
                                isPassed = false;
                                break;
                            }
                        }
                    }
                    
                    if (isPassed) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check for hanging pawns.
     * Two adjacent pawns on the 4th rank with no pawn support.
     */
    private static boolean hasHangingPawns(boolean[][] pawns) {
        int rank4 = 3; // 4th rank
        
        for (int file = 0; file < 7; file++) {
            if (pawns[file][rank4] && pawns[file + 1][rank4]) {
                // Check if they have no pawn support from behind
                boolean leftSupport = false;
                boolean rightSupport = false;
                
                if (file > 0) {
                    for (int rank = 0; rank < rank4; rank++) {
                        if (pawns[file - 1][rank]) leftSupport = true;
                    }
                }
                
                if (file < 6) {
                    for (int rank = 0; rank < rank4; rank++) {
                        if (pawns[file + 2][rank]) rightSupport = true;
                    }
                }
                
                if (!leftSupport && !rightSupport) return true;
            }
        }
        return false;
    }
}
