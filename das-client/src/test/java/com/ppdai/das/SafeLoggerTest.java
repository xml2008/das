package com.ppdai.das;

import com.google.common.collect.ImmutableMap;
import com.ppdai.das.core.DasLogger;
import com.ppdai.das.core.LogContext;
import com.ppdai.das.core.LogEntry;
import com.ppdai.das.core.NullLogger;
import com.ppdai.das.core.SafeLogger;
import com.ppdai.das.service.DasRequest;
import org.junit.Assert;
import org.junit.Test;

public class SafeLoggerTest {
    @Test
    public void testNormal() throws Exception {
        DasLogger dasLogger = new NullLogger();
        SafeLogger safeLogger = new SafeLogger(dasLogger);
        LogEntry.init();

        safeLogger.initialize(ImmutableMap.of());
        LogEntry logEntry = safeLogger.createLogEntry();
        safeLogger.startStatement(logEntry);
        safeLogger.endStatement(logEntry, null);
        safeLogger.success(logEntry, 1);
        safeLogger.fail(logEntry, null);
        safeLogger.start(logEntry);
        safeLogger.info("msg");
        safeLogger.warn("msg");
        safeLogger.error("msg", null);

        LogContext logContext = new LogContext();
        logContext.setAppId("appid");
        DasRequest dasRequest = new DasRequest();
        safeLogger.startRemoteRequest(dasRequest);
        safeLogger.receiveRemoteRequest(dasRequest);
        safeLogger.finishRemoteRequest(logContext, null);
        safeLogger.completeRemoteRequest(logContext, null);
        safeLogger.markdown(null);
        safeLogger.markup(null);
        safeLogger.startCrossShardTasks(logContext, true);
        safeLogger.endCrossShards(logContext, null);
        safeLogger.startTask(logContext, "shard");
        safeLogger.endTask(logContext, "shard", null);
        safeLogger.end(logContext, null);
        safeLogger.logTransaction("type", "name");
        safeLogger.completeTransaction(logContext, null);
        safeLogger.getConnectionFailed("db", null);
        safeLogger.shutdown();
        Assert.assertEquals("appid", logContext.getAppId());

    }
}
