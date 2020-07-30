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

import static com.ppdai.das.core.task.TaskType.QUERY;

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

    @Test
    public void testLogEntry() {
        LogEntry.init();
        LogEntry logEntry = new LogEntry();
        logEntry.setErrorMsg("em");
        logEntry.setException(new Exception("e"));
        logEntry.setSuccess(true);
        logEntry.setMethod("m");
        logEntry.setServerAddress("s");
        logEntry.setCommandType("c");
        logEntry.setUserName("u");
        logEntry.setAffectedRows(1);
        logEntry.setAffectedRowsArray(new int[]{1, 2});
        logEntry.setConnectionCost(2L);
        logEntry.setShardId("sid");
        logEntry.setTaskType(QUERY);
        Assert.assertEquals("N/A", logEntry.getStackTraceElement().getFileName());
        Assert.assertEquals("em", logEntry.getErrorMsg());
        Assert.assertEquals("e", logEntry.getException().getMessage());
        Assert.assertTrue(logEntry.isSuccess());
        Assert.assertFalse(logEntry.isTransactional());
        Assert.assertEquals(0, logEntry.getDuration());
        Assert.assertEquals("s", logEntry.getServerAddress());
        Assert.assertEquals("c", logEntry.getCommandType());
        Assert.assertEquals("u", logEntry.getUserName());
        Assert.assertEquals(1, logEntry.getAffectedRows().intValue());
        Assert.assertArrayEquals(new int[]{1, 2}, logEntry.getAffectedRowsArray());
        Assert.assertEquals(2, logEntry.getConnectionCost());
        Assert.assertEquals("sid", logEntry.getShardId());
        Assert.assertEquals(QUERY, logEntry.getTaskType());
        Assert.assertEquals(0, logEntry.getSqlSize());
    }
}
