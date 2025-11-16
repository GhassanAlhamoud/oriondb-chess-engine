package com.oriondb.model;

import java.util.*;

/**
 * Represents a chess game with metadata (tags) and moves.
 * Immutable after construction.
 */
public class Game {
    private final int id;
    private final Map<String, String> tags;
    private final List<Move> moves;
    
    public Game(int id, Map<String, String> tags, List<Move> moves) {
        this.id = id;
        this.tags = Collections.unmodifiableMap(new HashMap<>(tags));
        this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
    }
    
    public int getId() {
        return id;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public String getTag(String key) {
        return tags.get(key);
    }
    
    public String getTag(String key, String defaultValue) {
        return tags.getOrDefault(key, defaultValue);
    }
    
    public List<Move> getMoves() {
        return moves;
    }
    
    // Convenience methods for Seven Tag Roster
    public String getEvent() {
        return getTag("Event", "?");
    }
    
    public String getSite() {
        return getTag("Site", "?");
    }
    
    public String getDate() {
        return getTag("Date", "????.??.??");
    }
    
    public String getRound() {
        return getTag("Round", "?");
    }
    
    public String getWhite() {
        return getTag("White", "?");
    }
    
    public String getBlack() {
        return getTag("Black", "?");
    }
    
    public String getResult() {
        return getTag("Result", "*");
    }
    
    public Integer getWhiteElo() {
        String elo = getTag("WhiteElo");
        return elo != null && !elo.equals("?") ? Integer.parseInt(elo) : null;
    }
    
    public Integer getBlackElo() {
        String elo = getTag("BlackElo");
        return elo != null && !elo.equals("?") ? Integer.parseInt(elo) : null;
    }
    
    public String getEco() {
        return getTag("ECO");
    }
    
    @Override
    public String toString() {
        return String.format("Game #%d: %s vs %s (%s) - %s", 
            id, getWhite(), getBlack(), getDate(), getResult());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id == game.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
