package com.oriondb.parser;

import com.oriondb.model.Game;
import com.oriondb.model.Move;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Parser for PGN (Portable Game Notation) files.
 * Implements error-tolerant parsing that continues on malformed games.
 */
public class PgnParser {
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[\\s*(\\w+)\\s+\"([^\"]*)\"\\s*\\]");
    private static final Pattern MOVE_PATTERN = Pattern.compile(
        "([NBRQK]?[a-h]?[1-8]?x?[a-h][1-8](?:=[NBRQ])?[+#]?|O-O(?:-O)?[+#]?)"
    );
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\{([^}]*)\\}");
    private static final Pattern RESULT_PATTERN = Pattern.compile("(1-0|0-1|1/2-1/2|\\*)");
    
    private int gameIdCounter = 0;
    private int errorCount = 0;
    private List<String> errors = new ArrayList<>();
    
    /**
     * Parse all games from a PGN file.
     * 
     * @param file PGN file to parse
     * @return List of parsed games
     * @throws IOException if file cannot be read
     */
    public List<Game> parseFile(File file) throws IOException {
        List<Game> games = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            
            String gameText;
            while ((gameText = readNextGame(reader)) != null) {
                try {
                    Game game = parseGame(gameText);
                    if (game != null) {
                        games.add(game);
                    }
                } catch (Exception e) {
                    recordError("Error parsing game: " + e.getMessage());
                }
            }
        }
        
        return games;
    }
    
    /**
     * Read the next complete game from the reader.
     * A game consists of tag pairs followed by movetext.
     */
    private String readNextGame(BufferedReader reader) throws IOException {
        StringBuilder gameText = new StringBuilder();
        String line;
        boolean inGame = false;
        boolean foundTags = false;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Skip empty lines before game starts
            if (line.isEmpty() && !inGame) {
                continue;
            }
            
            // Tag pair indicates start of game
            if (line.startsWith("[")) {
                inGame = true;
                foundTags = true;
                gameText.append(line).append("\n");
            }
            // Movetext or result
            else if (inGame && !line.isEmpty()) {
                gameText.append(line).append(" ");
                
                // Check if this line contains a result marker
                if (RESULT_PATTERN.matcher(line).find()) {
                    // Game complete
                    return gameText.toString();
                }
            }
            // Empty line after tags/moves might indicate end of game
            else if (inGame && line.isEmpty() && foundTags) {
                // If we have content, return it
                if (gameText.length() > 0) {
                    return gameText.toString();
                }
            }
        }
        
        // Return remaining content if any
        return gameText.length() > 0 ? gameText.toString() : null;
    }
    
    /**
     * Parse a single game from PGN text.
     */
    private Game parseGame(String gameText) {
        Map<String, String> tags = new HashMap<>();
        List<Move> moves = new ArrayList<>();
        
        // Parse tags
        Matcher tagMatcher = TAG_PATTERN.matcher(gameText);
        int lastTagEnd = 0;
        while (tagMatcher.find()) {
            String key = tagMatcher.group(1);
            String value = tagMatcher.group(2);
            tags.put(key, value);
            lastTagEnd = tagMatcher.end();
        }
        
        // Ensure Seven Tag Roster defaults
        tags.putIfAbsent("Event", "?");
        tags.putIfAbsent("Site", "?");
        tags.putIfAbsent("Date", "????.??.??");
        tags.putIfAbsent("Round", "?");
        tags.putIfAbsent("White", "?");
        tags.putIfAbsent("Black", "?");
        tags.putIfAbsent("Result", "*");
        
        // Parse movetext (everything after tags)
        String movetext = gameText.substring(lastTagEnd).trim();
        
        // Remove move numbers (e.g., "1.", "23...", etc.)
        movetext = movetext.replaceAll("\\d+\\.+", " ");
        
        // Remove NAGs (Numeric Annotation Glyphs like $1, $2)
        movetext = movetext.replaceAll("\\$\\d+", " ");
        
        // Remove variations (recursive annotation variations in parentheses)
        movetext = removeVariations(movetext);
        
        // Extract moves and comments
        String currentComment = null;
        Matcher commentMatcher = COMMENT_PATTERN.matcher(movetext);
        Map<Integer, String> commentPositions = new HashMap<>();
        int commentIndex = 0;
        
        while (commentMatcher.find()) {
            String comment = commentMatcher.group(1).trim();
            commentPositions.put(commentMatcher.start(), comment);
        }
        
        // Remove comments from movetext for move extraction
        String movetextNoComments = COMMENT_PATTERN.matcher(movetext).replaceAll(" ");
        
        // Extract moves
        Matcher moveMatcher = MOVE_PATTERN.matcher(movetextNoComments);
        List<Integer> movePositions = new ArrayList<>();
        List<String> moveStrings = new ArrayList<>();
        
        while (moveMatcher.find()) {
            movePositions.add(moveMatcher.start());
            moveStrings.add(moveMatcher.group(1));
        }
        
        // Match comments to moves (comment after a move belongs to that move)
        for (int i = 0; i < moveStrings.size(); i++) {
            String moveSan = moveStrings.get(i);
            String comment = null;
            
            // Find comment between this move and next move
            int movePos = movePositions.get(i);
            int nextMovePos = (i + 1 < movePositions.size()) ? 
                movePositions.get(i + 1) : movetext.length();
            
            for (Map.Entry<Integer, String> entry : commentPositions.entrySet()) {
                if (entry.getKey() > movePos && entry.getKey() < nextMovePos) {
                    comment = entry.getValue();
                    break;
                }
            }
            
            moves.add(new Move(moveSan, comment));
        }
        
        // Create game
        int gameId = gameIdCounter++;
        return new Game(gameId, tags, moves);
    }
    
    /**
     * Remove variations (text in parentheses) from movetext.
     * Handles nested parentheses.
     */
    private String removeVariations(String text) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        
        for (char c : text.toCharArray()) {
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0) {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private void recordError(String error) {
        errorCount++;
        errors.add(error);
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public void clearErrors() {
        errorCount = 0;
        errors.clear();
    }
}
