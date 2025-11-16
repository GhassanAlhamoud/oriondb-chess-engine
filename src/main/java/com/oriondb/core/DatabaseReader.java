package com.oriondb.core;

import com.oriondb.model.Game;
import com.oriondb.model.Move;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Reads games from the OrionDB binary format.
 */
public class DatabaseReader implements AutoCloseable {
    private static final byte[] MAGIC_BYTES = {'O', 'R', 'D', 'B'};
    
    private final RandomAccessFile file;
    private final int version;
    private final int gameCount;
    
    public DatabaseReader(File dbFile) throws IOException {
        this.file = new RandomAccessFile(dbFile, "r");
        
        // Read and validate header
        byte[] magic = new byte[4];
        file.read(magic);
        if (!Arrays.equals(magic, MAGIC_BYTES)) {
            throw new IOException("Invalid database file: bad magic bytes");
        }
        
        this.version = file.readInt();
        if (version != 1) {
            throw new IOException("Unsupported database version: " + version);
        }
        
        this.gameCount = file.readInt();
    }
    
    /**
     * Read a game at a specific byte offset.
     */
    public Game readGameAt(long offset) throws IOException {
        file.seek(offset);
        
        // Read game length
        int gameLength = file.readInt();
        
        // Read game data
        byte[] gameData = new byte[gameLength];
        file.readFully(gameData);
        
        // Parse game data
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(gameData));
        
        // Read tags
        int tagCount = in.readInt();
        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < tagCount; i++) {
            String key = readString(in);
            String value = readString(in);
            tags.put(key, value);
        }
        
        // Read moves
        int moveCount = in.readInt();
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < moveCount; i++) {
            String san = readString(in);
            String comment = readString(in);
            moves.add(new Move(san, comment.isEmpty() ? null : comment));
        }
        
        // Extract game ID from offset (simplified - in production, store ID in game data)
        int gameId = (int) (offset / 1000); // Simplified ID generation
        
        return new Game(gameId, tags, moves);
    }
    
    /**
     * Read a length-prefixed string.
     */
    private String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length == 0) {
            return "";
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Get the total number of games in the database.
     */
    public int getGameCount() {
        return gameCount;
    }
    
    /**
     * Get the database version.
     */
    public int getVersion() {
        return version;
    }
    
    @Override
    public void close() throws IOException {
        file.close();
    }
}
