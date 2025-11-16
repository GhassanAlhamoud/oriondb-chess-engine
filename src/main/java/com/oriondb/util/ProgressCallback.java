package com.oriondb.util;

/**
 * Callback interface for reporting progress during database operations.
 */
@FunctionalInterface
public interface ProgressCallback {
    /**
     * Called periodically to report progress.
     * 
     * @param gamesProcessed Number of games processed so far
     * @param totalGames Total number of games (if known, -1 otherwise)
     * @param message Optional progress message
     */
    void onProgress(int gamesProcessed, int totalGames, String message);
    
    /**
     * No-op callback that does nothing.
     */
    ProgressCallback NOOP = (processed, total, message) -> {};
    
    /**
     * Console callback that prints progress to stdout.
     */
    ProgressCallback CONSOLE = (processed, total, message) -> {
        if (total > 0) {
            double percent = (processed * 100.0) / total;
            System.out.printf("Progress: %d/%d (%.1f%%) - %s%n", 
                processed, total, percent, message);
        } else {
            System.out.printf("Progress: %d games - %s%n", processed, message);
        }
    };
}
