package com.ppdai.das.core;

import java.util.Map;
import java.util.concurrent.Callable;

import com.ppdai.das.core.markdown.MarkDownInfo;
import com.ppdai.das.core.markdown.MarkupInfo;
import com.ppdai.das.core.task.SqlRequest;
import com.ppdai.das.service.DasRequest;

/**
 * A sandbox that prevent customized logger's exception break main flow
 *
 * TODO add async log capability
 *
 * @author jhhe
 *
 */
public class SafeLogger implements DasLogger {
    private DasLogger logger;

    public SafeLogger(DasLogger logger) {
        this.logger = logger;
    }

    @Override
    public void initialize(Map<String, String> settings) throws Exception {
        callLogger(() ->  logger.initialize(settings));
    }

    @Override
    public void info(String msg) {
        callLogger(() -> logger.info(msg));
    }

    @Override
    public void warn(String msg) {
        callLogger(() -> logger.warn(msg));
    }

    @Override
    public void error(String msg, Throwable e) {
        callLogger(() -> logger.error(msg, e));
    }

    @Override
    public void getConnectionFailed(String dbName, Throwable e) {
        callLogger(() -> logger.getConnectionFailed(dbName, e));
    }

    @Override
    public <T> LogContext start(SqlRequest<T> request) {
        return  callLogger(() -> logger.start(request));
    }

    @Override
    public void end(LogContext logContext, Throwable e) {
        callLogger(() ->  logger.end(logContext, e));
    }

    @Override
    public void startCrossShardTasks(LogContext logContext, boolean isSequentialExecution) {
        callLogger(() -> logger.startCrossShardTasks(logContext, isSequentialExecution));
    }

    @Override
    public void endCrossShards(LogContext logContext, Throwable e) {
        callLogger(() -> logger.endCrossShards(logContext, e));
    }

    @Override
    public void startTask(LogContext logContext, String shard) {
        callLogger(() -> logger.startTask(logContext, shard));
    }

    @Override
    public void endTask(LogContext logContext, String shard, Throwable e) {
        callLogger(() -> logger.endTask(logContext, shard, e));
    }

    @Override
    public LogEntry createLogEntry() {
       return callLogger(() -> logger.createLogEntry());
    }

    @Override
    public void start(LogEntry entry) {
        callLogger(() -> logger.start(entry));
    }

    @Override
    public void startStatement(LogEntry entry) {
        callLogger(() -> logger.startStatement(entry));
    }

    @Override
    public void endStatement(LogEntry entry, Throwable e) {
        callLogger(() -> logger.endStatement(entry, e));
    }

    @Override
    public void success(LogEntry entry, int count) {
        callLogger(() -> logger.success(entry, count));
    }

    @Override
    public void fail(LogEntry entry, Throwable e) {
        callLogger(() -> logger.fail(entry, e));
    }

    @Override
    public void markdown(MarkDownInfo markdown) {
        callLogger(() -> logger.markdown(markdown));
    }

    @Override
    public void markup(MarkupInfo markup) {
        callLogger(() -> logger.markup(markup));
    }

    @Override
    public void shutdown() {
        callLogger(() -> logger.shutdown());
    }

    @Override
    public LogContext startRemoteRequest(DasRequest dasRequest) {
        return callLogger(() -> logger.startRemoteRequest(dasRequest));
    }

    @Override
    public void completeRemoteRequest(LogContext logContext, Throwable e){
        logger.completeRemoteRequest(logContext, e);
    }

    @Override
    public LogContext receiveRemoteRequest(DasRequest dasRequest) {
        return logger.receiveRemoteRequest(dasRequest);
    }

    @Override
    public void finishRemoteRequest(LogContext logContext, Throwable e){
        logger.finishRemoteRequest(logContext, e);
    }

    @Override
    public LogContext logTransaction(String type, String name) {
        return logger.logTransaction(type, name);
    }

    @Override
    public void completeTransaction(LogContext logContext, Throwable throwable) {
        logger.completeTransaction(logContext, throwable);
    }

    @FunctionalInterface
    public interface CallWarpper {
        void run() throws Exception;
    }

    private void callLogger(CallWarpper f) {
        try{
            f.run();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private <T> T callLogger(Callable f) {
        try{
            return (T) f.call();
        }catch(Throwable e){
            e.printStackTrace();
            return null;
        }
    }
}
