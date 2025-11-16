package com.oriondb.index;

import com.oriondb.model.GamePosition;

import java.io.Serializable;
import java.util.*;

/**
 * Indexes moves for fast move-based queries.
 * Supports single move search and move sequence search.
 */
public class MoveIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map from SAN move to set of game positions where it occurred
    private final Map<String, Set<GamePosition>> moveToPositions;
    
    // Map from game ID to ordered list of moves (for sequence search)
    private final Map<Integer, List<MoveEntry>> gameToMoves;
    
    // Statistics
    private int totalMoves = 0;
    private int uniqueMoves = 0;
    
    public MoveIndex() {
        this.moveToPositions = new HashMap<>();
        this.gameToMoves = new HashMap<>();
    }
    
    /**
     * Represents a move in a game with its position.
     */
    public static class MoveEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final String san;
        public final int moveNumber;
        public final String fen;
        
        public MoveEntry(String san, int moveNumber, String fen) {
            this.san = san;
            this.moveNumber = moveNumber;
            this.fen = fen;
        }
    }
    
    /**
     * Add a move to the index.
     * 
     * @param san SAN notation of the move
     * @param gameId Game ID
     * @param moveNumber Move number (ply)
     * @param fen FEN string after the move
     */
    public void addMove(String san, int gameId, int moveNumber, String fen) {
        // Add to move-to-positions map
        GamePosition gamePos = new GamePosition(gameId, moveNumber, fen);
        moveToPositions.computeIfAbsent(san, k -> new HashSet<>()).add(gamePos);
        
        // Add to game-to-moves map
        MoveEntry entry = new MoveEntry(san, moveNumber, fen);
        gameToMoves.computeIfAbsent(gameId, k -> new ArrayList<>()).add(entry);
        
        totalMoves++;
        uniqueMoves = moveToPositions.size();
    }
    
    /**
     * Find all positions where a specific move was played.
     * 
     * @param san SAN notation of the move
     * @return Set of game positions
     */
    public Set<GamePosition> findMove(String san) {
        Set<GamePosition> positions = moveToPositions.get(san);
        return positions != null ? new HashSet<>(positions) : new HashSet<>();
    }
    
    /**
     * Find all positions where a move was played in a specific move number range.
     * 
     * @param san SAN notation of the move
     * @param minMove Minimum move number (inclusive)
     * @param maxMove Maximum move number (inclusive)
     * @return Set of game positions
     */
    public Set<GamePosition> findMoveInRange(String san, int minMove, int maxMove) {
        Set<GamePosition> allPositions = findMove(san);
        Set<GamePosition> filtered = new HashSet<>();
        
        for (GamePosition pos : allPositions) {
            if (pos.getMoveNumber() >= minMove && pos.getMoveNumber() <= maxMove) {
                filtered.add(pos);
            }
        }
        
        return filtered;
    }
    
    /**
     * Find games containing a specific move sequence.
     * 
     * @param sequence List of moves in SAN notation
     * @return Set of game IDs that contain the sequence
     */
    public Set<Integer> findSequence(List<String> sequence) {
        if (sequence == null || sequence.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<Integer> matchingGames = new HashSet<>();
        
        for (Map.Entry<Integer, List<MoveEntry>> entry : gameToMoves.entrySet()) {
            int gameId = entry.getKey();
            List<MoveEntry> moves = entry.getValue();
            
            if (containsSequence(moves, sequence)) {
                matchingGames.add(gameId);
            }
        }
        
        return matchingGames;
    }
    
    /**
     * Check if a list of moves contains a specific sequence.
     */
    private boolean containsSequence(List<MoveEntry> moves, List<String> sequence) {
        if (sequence.size() > moves.size()) {
            return false;
        }
        
        // Sliding window search
        for (int i = 0; i <= moves.size() - sequence.size(); i++) {
            boolean match = true;
            for (int j = 0; j < sequence.size(); j++) {
                if (!moves.get(i + j).san.equals(sequence.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Find games where a specific piece type made a move.
     * 
     * @param pieceType Piece type ('N', 'B', 'R', 'Q', 'K', or null for pawn)
     * @return Set of game IDs
     */
    public Set<Integer> findGamesByPieceMove(Character pieceType) {
        Set<Integer> games = new HashSet<>();
        
        for (Map.Entry<String, Set<GamePosition>> entry : moveToPositions.entrySet()) {
            String san = entry.getKey();
            
            // Check if move matches piece type
            boolean matches = false;
            if (pieceType == null) {
                // Pawn move (starts with lowercase or is a capture)
                matches = Character.isLowerCase(san.charAt(0)) || 
                         (san.length() > 1 && san.charAt(1) == 'x');
            } else {
                // Piece move (starts with piece letter)
                matches = san.charAt(0) == pieceType;
            }
            
            if (matches) {
                for (GamePosition pos : entry.getValue()) {
                    games.add(pos.getGameId());
                }
            }
        }
        
        return games;
    }
    
    /**
     * Get all moves made in a specific game.
     * 
     * @param gameId Game ID
     * @return List of move entries, or empty list if game not found
     */
    public List<MoveEntry> getGameMoves(int gameId) {
        List<MoveEntry> moves = gameToMoves.get(gameId);
        return moves != null ? new ArrayList<>(moves) : new ArrayList<>();
    }
    
    /**
     * Get the most common moves in the database.
     * 
     * @param limit Number of moves to return
     * @return List of moves sorted by frequency
     */
    public List<Map.Entry<String, Integer>> getMostCommonMoves(int limit) {
        Map<String, Integer> moveCounts = new HashMap<>();
        
        for (Map.Entry<String, Set<GamePosition>> entry : moveToPositions.entrySet()) {
            moveCounts.put(entry.getKey(), entry.getValue().size());
        }
        
        return moveCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get statistics about the move index.
     */
    public String getStats() {
        return String.format(
            "Move Index Statistics:\n" +
            "  Total moves indexed: %d\n" +
            "  Unique moves: %d\n" +
            "  Games indexed: %d",
            totalMoves,
            uniqueMoves,
            gameToMoves.size()
        );
    }
    
    /**
     * Get the number of games indexed.
     */
    public int getGameCount() {
        return gameToMoves.size();
    }
    
    /**
     * Get the number of unique moves.
     */
    public int getUniqueMoveCount() {
        return uniqueMoves;
    }
    
    /**
     * Get the total number of moves indexed.
     */
    public int getTotalMoveCount() {
        return totalMoves;
    }
}
