package com.oriondb.query.cql;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for Chess Query Language (CQL).
 * Tokenizes CQL query strings.
 */
public class CQLLexer {
    public enum TokenType {
        // Literals
        STRING, NUMBER, IDENTIFIER,
        
        // Operators
        EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, GREATER_EQUALS, LESS_EQUALS,
        
        // Keywords
        AND, OR, CONTAINS,
        
        // Punctuation
        LEFT_PAREN, RIGHT_PAREN,
        
        // End of input
        EOF
    }
    
    public static class Token {
        public final TokenType type;
        public final String value;
        public final int position;
        
        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }
        
        @Override
        public String toString() {
            return type + "(" + value + ")";
        }
    }
    
    private final String input;
    private int position = 0;
    
    public CQLLexer(String input) {
        this.input = input != null ? input : "";
    }
    
    /**
     * Tokenize the input string.
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        while (position < input.length()) {
            skipWhitespace();
            if (position >= input.length()) break;
            
            char c = input.charAt(position);
            
            // String literals
            if (c == '\'' || c == '"') {
                tokens.add(readString(c));
            }
            // Numbers
            else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            }
            // Identifiers and keywords
            else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
            }
            // Operators
            else if (c == '=') {
                tokens.add(new Token(TokenType.EQUALS, "=", position));
                position++;
            }
            else if (c == '!' && peek() == '=') {
                tokens.add(new Token(TokenType.NOT_EQUALS, "!=", position));
                position += 2;
            }
            else if (c == '>' && peek() == '=') {
                tokens.add(new Token(TokenType.GREATER_EQUALS, ">=", position));
                position += 2;
            }
            else if (c == '>') {
                tokens.add(new Token(TokenType.GREATER_THAN, ">", position));
                position++;
            }
            else if (c == '<' && peek() == '=') {
                tokens.add(new Token(TokenType.LESS_EQUALS, "<=", position));
                position += 2;
            }
            else if (c == '<') {
                tokens.add(new Token(TokenType.LESS_THAN, "<", position));
                position++;
            }
            // Parentheses
            else if (c == '(') {
                tokens.add(new Token(TokenType.LEFT_PAREN, "(", position));
                position++;
            }
            else if (c == ')') {
                tokens.add(new Token(TokenType.RIGHT_PAREN, ")", position));
                position++;
            }
            else {
                throw new RuntimeException("Unexpected character at position " + position + ": " + c);
            }
        }
        
        tokens.add(new Token(TokenType.EOF, "", position));
        return tokens;
    }
    
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }
    
    private char peek() {
        return position + 1 < input.length() ? input.charAt(position + 1) : '\0';
    }
    
    private Token readString(char quote) {
        int start = position;
        position++; // Skip opening quote
        
        StringBuilder value = new StringBuilder();
        while (position < input.length() && input.charAt(position) != quote) {
            value.append(input.charAt(position));
            position++;
        }
        
        if (position < input.length()) {
            position++; // Skip closing quote
        }
        
        return new Token(TokenType.STRING, value.toString(), start);
    }
    
    private Token readNumber() {
        int start = position;
        StringBuilder value = new StringBuilder();
        
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            value.append(input.charAt(position));
            position++;
        }
        
        return new Token(TokenType.NUMBER, value.toString(), start);
    }
    
    private Token readIdentifierOrKeyword() {
        int start = position;
        StringBuilder value = new StringBuilder();
        
        while (position < input.length() && 
               (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            value.append(input.charAt(position));
            position++;
        }
        
        String str = value.toString();
        TokenType type;
        
        // Check for keywords
        switch (str.toUpperCase()) {
            case "AND":
                type = TokenType.AND;
                break;
            case "OR":
                type = TokenType.OR;
                break;
            case "CONTAINS":
                type = TokenType.CONTAINS;
                break;
            default:
                type = TokenType.IDENTIFIER;
        }
        
        return new Token(type, str, start);
    }
}
