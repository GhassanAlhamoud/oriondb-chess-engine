package com.oriondb.query.cql;

import com.oriondb.query.SearchBuilder;
import com.oriondb.query.cql.ast.*;

/**
 * Compiles CQL AST to SearchBuilder calls.
 */
public class CQLCompiler implements ExpressionVisitor<SearchBuilder> {
    private final SearchBuilder baseBuilder;
    
    public CQLCompiler(SearchBuilder baseBuilder) {
        this.baseBuilder = baseBuilder;
    }
    
    /**
     * Compile an expression and return a configured SearchBuilder.
     */
    public SearchBuilder compile(Expression expression) {
        return expression.accept(this);
    }
    
    @Override
    public SearchBuilder visitBinaryExpression(BinaryExpression expr) {
        // For AND: apply both conditions to the same builder
        // For OR: would need union support (simplified here)
        SearchBuilder builder = expr.getLeft().accept(this);
        
        if (expr.getOperator() == BinaryExpression.Operator.AND) {
            // Apply right side to the same builder
            expr.getRight().accept(this);
        }
        // OR would require more complex handling
        
        return builder;
    }
    
    @Override
    public SearchBuilder visitComparisonExpression(ComparisonExpression expr) {
        String fieldName = expr.getField().getFieldName().toLowerCase();
        ComparisonExpression.Operator operator = expr.getOperator();
        Literal value = expr.getValue();
        
        switch (fieldName) {
            case "player":
                if (operator == ComparisonExpression.Operator.EQUALS) {
                    baseBuilder.withPlayer(value.asString());
                }
                break;
                
            case "event":
                if (operator == ComparisonExpression.Operator.EQUALS) {
                    baseBuilder.withEvent(value.asString());
                }
                break;
                
            case "eco":
                if (operator == ComparisonExpression.Operator.EQUALS) {
                    baseBuilder.withEco(value.asString());
                }
                break;
                
            case "result":
                if (operator == ComparisonExpression.Operator.EQUALS) {
                    baseBuilder.withResult(value.asString());
                }
                break;
                
            case "elo":
                int eloValue = value.asInt();
                switch (operator) {
                    case GREATER_THAN:
                        baseBuilder.withMinElo(eloValue + 1);
                        break;
                    case GREATER_EQUALS:
                        baseBuilder.withMinElo(eloValue);
                        break;
                    case LESS_THAN:
                        baseBuilder.withMaxElo(eloValue - 1);
                        break;
                    case LESS_EQUALS:
                        baseBuilder.withMaxElo(eloValue);
                        break;
                    case EQUALS:
                        baseBuilder.withEloRange(eloValue, eloValue);
                        break;
                }
                break;
                
            case "date":
                String dateValue = value.asString();
                switch (operator) {
                    case GREATER_EQUALS:
                        baseBuilder.withDateRange(dateValue, "9999.99.99");
                        break;
                    case LESS_EQUALS:
                        baseBuilder.withDateRange("0000.00.00", dateValue);
                        break;
                    case EQUALS:
                        baseBuilder.withDateRange(dateValue, dateValue);
                        break;
                }
                break;
                
            case "fen":
                // Phase 2 feature - requires extended SearchBuilder
                // if (operator == ComparisonExpression.Operator.EQUALS) {
                //     baseBuilder.withFen(value.asString());
                // }
                break;
                
            case "structure":
                // Phase 2 feature - requires extended SearchBuilder
                // if (operator == ComparisonExpression.Operator.EQUALS) {
                //     baseBuilder.withPawnStructure(value.asString());
                // }
                break;
                
            case "commentary":
                // Phase 3 feature - requires extended SearchBuilder
                // if (operator == ComparisonExpression.Operator.CONTAINS) {
                //     baseBuilder.withCommentary(value.asString());
                // }
                break;
        }
        
        return baseBuilder;
    }
    
    @Override
    public SearchBuilder visitFieldReference(FieldReference expr) {
        // Field references are handled in comparison expressions
        return baseBuilder;
    }
    
    @Override
    public SearchBuilder visitLiteral(Literal expr) {
        // Literals are handled in comparison expressions
        return baseBuilder;
    }
}
