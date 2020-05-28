package com.ppdai.das.client.sqlbuilder;

import com.ppdai.das.strategy.ColumnCondition;
import com.ppdai.das.strategy.Condition;
import com.ppdai.das.strategy.ConditionProvider;
import com.ppdai.das.strategy.OperatorEnum;

public class BooleanExpression extends Expression implements ConditionProvider {
    private String template;
    
    public static final BooleanExpression TRUE = new BooleanExpression("TRUE");

    public static final BooleanExpression FALSE = new BooleanExpression("FALSE");

    private BooleanExpression(String template) {
        this.template = template;;
    }

    @Override
    public Expression when(boolean condition) {
        throw new IllegalStateException("when() can not be applied to TRUE/FALSE");
    }
    
    @Override
    public boolean isIncluded() {
        return true;
    }

    @Override
    public String build(BuilderContext helper) {
        return template;
    }

    @Override
    public Condition build() {
        return new ColumnCondition(OperatorEnum.UN_DEFINED, "", "", null);
    }
}