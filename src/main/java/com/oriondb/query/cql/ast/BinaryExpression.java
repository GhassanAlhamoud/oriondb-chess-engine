package com.oriondb.query.cql.ast;

/**
 * Binary expression (AND, OR).
 */
public class BinaryExpression implements Expression {
    public enum Operator {
        AND, OR
    }
    
    private final Expression left;
    private final Operator operator;
    private final Expression right;
    
    public BinaryExpression(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public Expression getLeft() {
        return left;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Expression getRight() {
        return right;
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
    
    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
