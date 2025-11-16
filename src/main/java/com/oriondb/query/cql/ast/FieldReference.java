package com.oriondb.query.cql.ast;

/**
 * Field reference (e.g., player, eco, elo).
 */
public class FieldReference implements Expression {
    private final String fieldName;
    
    public FieldReference(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitFieldReference(this);
    }
    
    @Override
    public String toString() {
        return fieldName;
    }
}
