package com.oriondb.core;

import com.oriondb.model.Game;
import com.oriondb.query.SearchBuilder;
import com.oriondb.query.cql.*;
import com.oriondb.query.cql.ast.Expression;

import java.io.IOException;
import java.util.List;

/**
 * Chess Query Language (CQL) interface for OrionDB.
 * Allows querying with a single string instead of fluent API.
 */
public class CQLQuery {
    private final SearchBuilder searchBuilder;
    
    public CQLQuery(SearchBuilder searchBuilder) {
        this.searchBuilder = searchBuilder;
    }
    
    /**
     * Execute a CQL query string.
     * 
     * Examples:
     * - "player='Carlsen' AND elo > 2700"
     * - "eco='B90' AND result='1-0'"
     * - "commentary CONTAINS 'novelty' AND elo >= 2600"
     * 
     * @param cql CQL query string
     * @return List of matching games
     * @throws IOException if database read fails
     */
    public List<Game> query(String cql) throws IOException {
        // Tokenize
        CQLLexer lexer = new CQLLexer(cql);
        List<CQLLexer.Token> tokens = lexer.tokenize();
        
        // Parse
        CQLParser parser = new CQLParser(tokens);
        Expression ast = parser.parse();
        
        // Compile to SearchBuilder
        CQLCompiler compiler = new CQLCompiler(searchBuilder);
        SearchBuilder configuredBuilder = compiler.compile(ast);
        
        // Execute
        return configuredBuilder.execute();
    }
    
    /**
     * Count matching games without loading them.
     */
    public int count(String cql) {
        try {
            CQLLexer lexer = new CQLLexer(cql);
            List<CQLLexer.Token> tokens = lexer.tokenize();
            
            CQLParser parser = new CQLParser(tokens);
            Expression ast = parser.parse();
            
            CQLCompiler compiler = new CQLCompiler(searchBuilder);
            SearchBuilder configuredBuilder = compiler.compile(ast);
            
            return configuredBuilder.count();
        } catch (Exception e) {
            return 0;
        }
    }
}
