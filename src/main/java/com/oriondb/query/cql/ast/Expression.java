package com.oriondb.query.cql.ast;

/**
 * Base interface for CQL expression AST nodes.
 */
public interface Expression {
    /**
     * Accept a visitor for processing this expression.
     */
    <T> T accept(ExpressionVisitor<T> visitor);
}
