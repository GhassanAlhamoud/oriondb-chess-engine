package com.oriondb.model;

/**
 * Represents a single chess move in Standard Algebraic Notation (SAN).
 * Stores both the SAN string and optional comment.
 */
public class Move {
    private final String san;
    private final String comment;
    
    public Move(String san) {
        this(san, null);
    }
    
    public Move(String san, String comment) {
        this.san = san;
        this.comment = comment;
    }
    
    public String getSan() {
        return san;
    }
    
    public String getComment() {
        return comment;
    }
    
    public boolean hasComment() {
        return comment != null && !comment.isEmpty();
    }
    
    @Override
    public String toString() {
        return hasComment() ? san + " {" + comment + "}" : san;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return san.equals(move.san);
    }
    
    @Override
    public int hashCode() {
        return san.hashCode();
    }
}
