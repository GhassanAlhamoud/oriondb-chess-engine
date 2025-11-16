package com.oriondb.index;

import com.oriondb.model.GamePosition;

import java.io.Serializable;
import java.util.*;

/**
 * Simplified index for comment-based searches.
 * In a full implementation, this would use Apache Lucene.
 */
public class CommentIndex implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Simple word-based index for demonstration
    private final Map<String, Set<GamePosition>> wordToPositions;
    
    public CommentIndex() {
        this.wordToPositions = new HashMap<>();
    }
    
    /**
     * Add a comment to the index.
     */
    public void addComment(String comment, GamePosition position) {
        if (comment == null || comment.isEmpty()) {
            return;
        }
        
        // Simple tokenization (split on whitespace and punctuation)
        String[] words = comment.toLowerCase().split("[\\s,\\.!\\?;:]+");
        
        for (String word : words) {
            if (word.length() > 2) { // Ignore very short words
                wordToPositions.computeIfAbsent(word, k -> new HashSet<>())
                              .add(position);
            }
        }
    }
    
    /**
     * Search for positions with comments containing the given text.
     */
    public List<GamePosition> search(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return Collections.emptyList();
        }
        
        String[] words = searchText.toLowerCase().split("\\s+");
        
        if (words.length == 0) {
            return Collections.emptyList();
        }
        
        // Find positions containing all words (AND logic)
        Set<GamePosition> result = null;
        
        for (String word : words) {
            Set<GamePosition> positions = wordToPositions.get(word);
            
            if (positions == null || positions.isEmpty()) {
                return Collections.emptyList(); // No matches for this word
            }
            
            if (result == null) {
                result = new HashSet<>(positions);
            } else {
                result.retainAll(positions); // Intersection
            }
        }
        
        return result != null ? new ArrayList<>(result) : Collections.emptyList();
    }
    
    /**
     * Get statistics about the index.
     */
    public String getStats() {
        return String.format(
            "Comment Index Statistics:\n" +
            "  Unique words: %d",
            wordToPositions.size()
        );
    }
}
