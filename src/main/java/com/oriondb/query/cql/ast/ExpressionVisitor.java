package com.oriondb.query.cql.ast;

/**
 * Visitor interface for CQL expression AST nodes.
 */
public interface ExpressionVisitor<T> {
    T visitBinaryExpression(BinaryExpression expr);
    T visitComparisonExpression(ComparisonExpression expr);
    T visitFieldReference(FieldReference expr);
    T visitLiteral(Literal expr);
}
