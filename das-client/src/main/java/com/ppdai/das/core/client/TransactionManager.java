package com.ppdai.das.core.client;

import com.ppdai.das.client.Hints;

import java.sql.SQLException;

public abstract class TransactionManager<T> {

    protected final static ThreadLocal transactionHolder = new ThreadLocal<>();

    public abstract Object doInTransaction(T action, Hints hints) throws SQLException;

    public static boolean isInTransaction() {
        return transactionHolder.get() != null;
    }

    public static <TX> TX getCurrentTransaction() {
        return (TX) transactionHolder.get();
    }

    public static void clearCurrentTransaction() {
        transactionHolder.set(null);
    }
}
