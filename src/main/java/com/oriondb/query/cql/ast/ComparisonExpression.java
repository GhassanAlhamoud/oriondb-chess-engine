package com.oriondb.query.cql.ast;

/**
 * Comparison expression (=, !=, >, <, >=, <=, CONTAINS).
 */
public class ComparisonExpression implements Expression {
    public enum Operator {
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_EQUALS(">="),
        LESS_EQUALS("<="),
        CONTAINS("CONTAINS");
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        @Override
        public String toString() {
            return symbol;
        }
    }
    
    private final FieldReference field;
    private final Operator operator;
    private final Literal value;
    
    public ComparisonExpression(FieldReference field, Operator operator, Literal value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    public FieldReference getField() {
        return field;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Literal getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitComparisonExpression(this);
    }
    
    @Override
    public String toString() {
        return field + " " + operator + " " + value;
    }
}
