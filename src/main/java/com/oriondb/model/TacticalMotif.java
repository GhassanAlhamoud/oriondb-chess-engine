package com.oriondb.model;

/**
 * Enumeration of tactical motifs that can be detected in chess positions.
 */
public enum TacticalMotif {
    /**
     * A piece is pinned and cannot move without exposing a more valuable piece.
     */
    PIN("Pin"),
    
    /**
     * A piece attacks two or more enemy pieces simultaneously.
     */
    FORK("Fork"),
    
    /**
     * A valuable piece is attacked and must move, exposing a less valuable piece behind it.
     */
    SKEWER("Skewer"),
    
    /**
     * Moving a piece reveals an attack from another piece behind it.
     */
    DISCOVERED_ATTACK("Discovered Attack"),
    
    /**
     * Two pieces attack the same target.
     */
    DOUBLE_ATTACK("Double Attack"),
    
    /**
     * Material is sacrificed for tactical or positional advantage.
     */
    SACRIFICE("Sacrifice"),
    
    /**
     * A piece is forced away from defending another piece or square.
     */
    DEFLECTION("Deflection"),
    
    /**
     * A piece is lured to a bad square.
     */
    DECOY("Decoy"),
    
    /**
     * A defending piece is removed, leaving another piece undefended.
     */
    REMOVAL_OF_DEFENDER("Removal of Defender"),
    
    /**
     * A piece blocks a line of defense or attack.
     */
    INTERFERENCE("Interference"),
    
    /**
     * A piece is given too many defensive tasks.
     */
    OVERLOADING("Overloading"),
    
    /**
     * Any move worsens the position (zugzwang).
     */
    ZUGZWANG("Zugzwang");
    
    private final String displayName;
    
    TacticalMotif(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
