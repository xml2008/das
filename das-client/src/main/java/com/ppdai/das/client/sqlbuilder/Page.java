package com.ppdai.das.client.sqlbuilder;

import com.ppdai.das.client.Parameter;
import com.ppdai.das.client.ParameterDefinition;

import java.util.Arrays;
import java.util.List;

public class Page extends Template {
    public static final String EMPTY = "PAGE_TEMPLATE_NOT_SPECIFIED? ?";

    private int pageNo;
    private int pageSize;

    public Page(int pageNo, int pageSize) {
        super(EMPTY, Parameter.integerOf("", (pageNo - 1) * pageSize), Parameter.integerOf("", pageSize));
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Page(String template, ParameterDefinition pageNo, ParameterDefinition pageSize) {
        super(EMPTY, pageNo, pageSize);
    }

    @Override
    public String build(BuilderContext context) {
        return context.getPageTemplate();
    }

    public void reWritePage(int shardNumber) {
        if(shardNumber < 2) {
            return;
        }

        List<Parameter> parameters = buildParameters();
        parameters.clear();
        parameters.addAll(Arrays.asList(Parameter.integerOf("", (pageNo/shardNumber - 1) * (pageSize/shardNumber)), Parameter.integerOf("", pageSize/shardNumber)));
    }
}
