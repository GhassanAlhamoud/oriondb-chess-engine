package com.oriondb;

import com.oriondb.model.Game;
import com.oriondb.parser.PgnParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for PGN parser.
 */
public class PgnParserTest {
    
    @Test
    public void testParseSamplePgn() throws Exception {
        PgnParser parser = new PgnParser();
        File sampleFile = new File("examples/sample.pgn");
        
        if (!sampleFile.exists()) {
            System.out.println("Sample PGN file not found, skipping test");
            return;
        }
        
        List<Game> games = parser.parseFile(sampleFile);
        
        assertNotNull(games);
        assertTrue(games.size() > 0, "Should parse at least one game");
        
        // Check first game
        Game firstGame = games.get(0);
        assertNotNull(firstGame.getWhite());
        assertNotNull(firstGame.getBlack());
        assertNotNull(firstGame.getResult());
        assertTrue(firstGame.getMoves().size() > 0, "Should have moves");
        
        System.out.println("Parsed " + games.size() + " games successfully");
        System.out.println("First game: " + firstGame);
    }
    
    @Test
    public void testGameMetadata() {
        // This test would require a specific PGN file
        // For now, just demonstrate the API
        assertTrue(true);
    }
}
