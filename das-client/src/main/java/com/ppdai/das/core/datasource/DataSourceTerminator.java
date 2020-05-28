package com.ppdai.das.core.datasource;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ppdai.das.core.helper.CustomThreadFactory;

public class DataSourceTerminator {
    private static volatile DataSourceTerminator terminator = null;
    private ScheduledExecutorService service =
            new ScheduledThreadPoolExecutor(POOL_SIZE, new CustomThreadFactory(THREAD_NAME));

    private static final int INIT_DELAY = 0;
    private static final int POOL_SIZE = 4;
    private static final String THREAD_NAME = "DataSourceTerminator";

    public synchronized static DataSourceTerminator getInstance() {
        if (terminator == null) {
            terminator = new DataSourceTerminator();
        }
        return terminator;
    }

    public void close(final SingleDataSource oldDataSource) {
        DefaultDataSourceTerminateTask task = new DefaultDataSourceTerminateTask(oldDataSource);
        service.schedule(task, INIT_DELAY, TimeUnit.MILLISECONDS);
    }

}
