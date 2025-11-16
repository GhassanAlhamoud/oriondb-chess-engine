package com.oriondb.query.cql;

import com.oriondb.query.cql.ast.*;

import java.util.List;

/**
 * Parser for Chess Query Language (CQL).
 * Builds an Abstract Syntax Tree from tokens.
 */
public class CQLParser {
    private final List<CQLLexer.Token> tokens;
    private int position = 0;
    
    public CQLParser(List<CQLLexer.Token> tokens) {
        this.tokens = tokens;
    }
    
    /**
     * Parse the tokens into an expression AST.
     */
    public Expression parse() {
        return parseOrExpression();
    }
    
    /**
     * Parse OR expression (lowest precedence).
     */
    private Expression parseOrExpression() {
        Expression left = parseAndExpression();
        
        while (match(CQLLexer.TokenType.OR)) {
            Expression right = parseAndExpression();
            left = new BinaryExpression(left, BinaryExpression.Operator.OR, right);
        }
        
        return left;
    }
    
    /**
     * Parse AND expression.
     */
    private Expression parseAndExpression() {
        Expression left = parsePrimaryExpression();
        
        while (match(CQLLexer.TokenType.AND)) {
            Expression right = parsePrimaryExpression();
            left = new BinaryExpression(left, BinaryExpression.Operator.AND, right);
        }
        
        return left;
    }
    
    /**
     * Parse primary expression (comparison or parenthesized expression).
     */
    private Expression parsePrimaryExpression() {
        // Parenthesized expression
        if (match(CQLLexer.TokenType.LEFT_PAREN)) {
            Expression expr = parseOrExpression();
            expect(CQLLexer.TokenType.RIGHT_PAREN);
            return expr;
        }
        
        // Comparison expression
        return parseComparisonExpression();
    }
    
    /**
     * Parse comparison expression (field operator value).
     */
    private Expression parseComparisonExpression() {
        // Field name
        CQLLexer.Token fieldToken = expect(CQLLexer.TokenType.IDENTIFIER);
        FieldReference field = new FieldReference(fieldToken.value);
        
        // Operator
        ComparisonExpression.Operator operator;
        if (match(CQLLexer.TokenType.EQUALS)) {
            operator = ComparisonExpression.Operator.EQUALS;
        } else if (match(CQLLexer.TokenType.NOT_EQUALS)) {
            operator = ComparisonExpression.Operator.NOT_EQUALS;
        } else if (match(CQLLexer.TokenType.GREATER_EQUALS)) {
            operator = ComparisonExpression.Operator.GREATER_EQUALS;
        } else if (match(CQLLexer.TokenType.GREATER_THAN)) {
            operator = ComparisonExpression.Operator.GREATER_THAN;
        } else if (match(CQLLexer.TokenType.LESS_EQUALS)) {
            operator = ComparisonExpression.Operator.LESS_EQUALS;
        } else if (match(CQLLexer.TokenType.LESS_THAN)) {
            operator = ComparisonExpression.Operator.LESS_THAN;
        } else if (match(CQLLexer.TokenType.CONTAINS)) {
            operator = ComparisonExpression.Operator.CONTAINS;
        } else {
            throw new RuntimeException("Expected comparison operator at position " + current().position);
        }
        
        // Value
        Literal value;
        if (check(CQLLexer.TokenType.STRING)) {
            value = new Literal(advance().value);
        } else if (check(CQLLexer.TokenType.NUMBER)) {
            value = new Literal(Integer.parseInt(advance().value));
        } else {
            throw new RuntimeException("Expected value at position " + current().position);
        }
        
        return new ComparisonExpression(field, operator, value);
    }
    
    /**
     * Check if current token matches the given type.
     */
    private boolean check(CQLLexer.TokenType type) {
        return current().type == type;
    }
    
    /**
     * If current token matches, advance and return true.
     */
    private boolean match(CQLLexer.TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }
    
    /**
     * Expect a specific token type and advance.
     */
    private CQLLexer.Token expect(CQLLexer.TokenType type) {
        if (check(type)) {
            return advance();
        }
        throw new RuntimeException("Expected " + type + " at position " + current().position + 
                                 ", but got " + current().type);
    }
    
    /**
     * Get current token.
     */
    private CQLLexer.Token current() {
        return tokens.get(position);
    }
    
    /**
     * Advance to next token.
     */
    private CQLLexer.Token advance() {
        if (position < tokens.size() - 1) {
            position++;
        }
        return tokens.get(position - 1);
    }
}
