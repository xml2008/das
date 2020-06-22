package com.ppdai.das.client.sqlbuilder;

import com.ppdai.das.strategy.ColumnCondition;
import com.ppdai.das.strategy.Condition;
import com.ppdai.das.strategy.ConditionProvider;
import com.ppdai.das.strategy.OperatorEnum;

public class CustomizedExpression extends Expression implements ConditionProvider {
    private String template;
    
    public CustomizedExpression(String template) {
        this.template = template;;
    }

    @Override
    public String build(BuilderContext helper) {
        return template;
    }

    String getTemplate() {
        return template;
    }

    @Override
    public String toString() {
        return build(new DefaultBuilderContext());
    }

    @Override
    public Condition build() {
        return new ColumnCondition(OperatorEnum.UN_DEFINED, "", "", null);
    }
}