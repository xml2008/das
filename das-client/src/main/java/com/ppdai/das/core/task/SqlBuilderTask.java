package com.ppdai.das.core.task;

import java.sql.SQLException;
import java.util.List;

import com.ppdai.das.client.Hints;
import com.ppdai.das.client.Parameter;
import com.ppdai.das.core.client.DasDirectClient;

public interface SqlBuilderTask<T> {
    T execute(DasDirectClient client, StatementConditionProvider provider, List<Parameter> parameters, Hints hints) throws SQLException;
}
