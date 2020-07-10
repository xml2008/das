package com.ppdai.das.core.task;

import java.sql.SQLException;
import java.util.List;

import com.ppdai.das.client.Parameter;
import com.ppdai.das.core.client.DirectClient;
import com.ppdai.das.client.Hints;

public interface SqlTask<T> {
	T execute(DirectClient client, String sql, List<Parameter> parameters, Hints hints) throws SQLException;
}
