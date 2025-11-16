package com.oriondb.query.cql.ast;

/**
 * Literal value (string, number).
 */
public class Literal implements Expression {
    private final Object value;
    
    public Literal(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }
    
    public String asString() {
        return value != null ? value.toString() : "";
    }
    
    public int asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
    
    @Override
    public String toString() {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return value != null ? value.toString() : "null";
    }
}
