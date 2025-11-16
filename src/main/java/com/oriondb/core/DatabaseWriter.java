package com.oriondb.core;

import com.oriondb.model.Game;
import com.oriondb.model.Move;
import com.oriondb.index.IndexManager;
import com.oriondb.index.IndexBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Writes games to the OrionDB binary format.
 * Format:
 * - Header: magic bytes, version, game count
 * - Game data: sequential game records
 * - Each game: length, tags, moves, comments
 */
public class DatabaseWriter implements AutoCloseable {
    private static final byte[] MAGIC_BYTES = {'O', 'R', 'D', 'B'};
    private static final int VERSION = 1;
    
    private final DataOutputStream out;
    private final IndexBuilder indexBuilder;
    private int gamesWritten = 0;
    
    public DatabaseWriter(File file) throws IOException {
        this(file, false, false);
    }
    
    public DatabaseWriter(File file, boolean enablePositionIndexing, boolean enableCommentIndexing) throws IOException {
        this.out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        this.indexBuilder = new IndexBuilder(enablePositionIndexing, enableCommentIndexing);
        writeHeader();
    }
    
    /**
     * Write the database header.
     */
    private void writeHeader() throws IOException {
        out.write(MAGIC_BYTES);
        out.writeInt(VERSION);
        out.writeInt(0); // Game count placeholder, will be updated at close
    }
    
    /**
     * Write a single game to the database.
     * Returns the byte offset where the game was written.
     */
    public long writeGame(Game game) throws IOException {
        long offset = getPosition();
        
        // Write game to a byte array first to know its length
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream gameOut = new DataOutputStream(buffer);
        
        // Write tags
        Map<String, String> tags = game.getTags();
        gameOut.writeInt(tags.size());
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            writeString(gameOut, entry.getKey());
            writeString(gameOut, entry.getValue());
        }
        
        // Write moves
        gameOut.writeInt(game.getMoves().size());
        for (Move move : game.getMoves()) {
            writeString(gameOut, move.getSan());
            writeString(gameOut, move.getComment() != null ? move.getComment() : "");
        }
        
        gameOut.flush();
        byte[] gameData = buffer.toByteArray();
        
        // Write game length and data to main file
        out.writeInt(gameData.length);
        out.write(gameData);
        
        // Index the game with all indexes
        indexBuilder.indexGame(game, offset);
        gamesWritten++;
        
        return offset;
    }
    
    /**
     * Write a string with length prefix.
     */
    private void writeString(DataOutputStream out, String str) throws IOException {
        if (str == null) {
            out.writeInt(0);
        } else {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }
    
    /**
     * Get current position in the output stream.
     */
    private long getPosition() throws IOException {
        // For DataOutputStream, we need to track position manually
        // This is a simplified version; in production, use a CountingOutputStream
        return out.size();
    }
    
    /**
     * Get the index builder.
     */
    public IndexBuilder getIndexBuilder() {
        return indexBuilder;
    }
    
    /**
     * Get the metadata index manager.
     */
    public IndexManager getIndexManager() {
        return indexBuilder.getMetadataIndex();
    }
    
    /**
     * Get number of games written.
     */
    public int getGamesWritten() {
        return gamesWritten;
    }
    
    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }
}
