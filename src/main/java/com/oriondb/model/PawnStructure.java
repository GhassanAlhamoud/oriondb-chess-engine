package com.oriondb.model;

/**
 * Common pawn structures in chess.
 */
public enum PawnStructure {
    /**
     * Isolated Queen's Pawn - a pawn on d4/d5 with no pawns on c and e files.
     */
    IQP("Isolated Queen's Pawn"),
    
    /**
     * Carlsbad structure - typical in Queen's Gambit Declined.
     */
    CARLSBAD("Carlsbad Structure"),
    
    /**
     * Maroczy Bind - pawns on c4 and e4 controlling d5.
     */
    MAROCZY_BIND("Maroczy Bind"),
    
    /**
     * Hanging pawns - two adjacent pawns on the 4th rank with no pawn support.
     */
    HANGING_PAWNS("Hanging Pawns"),
    
    /**
     * Pawn chain - diagonal chain of pawns.
     */
    PAWN_CHAIN("Pawn Chain"),
    
    /**
     * Doubled pawns - two pawns of the same color on the same file.
     */
    DOUBLED_PAWNS("Doubled Pawns"),
    
    /**
     * Passed pawn - a pawn with no enemy pawns to stop it.
     */
    PASSED_PAWN("Passed Pawn"),
    
    /**
     * Backward pawn - a pawn that cannot advance safely.
     */
    BACKWARD_PAWN("Backward Pawn"),
    
    /**
     * No specific structure identified.
     */
    NONE("No Structure");
    
    private final String description;
    
    PawnStructure(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
