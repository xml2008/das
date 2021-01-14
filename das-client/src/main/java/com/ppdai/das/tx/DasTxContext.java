package com.ppdai.das.tx;

public class DasTxContext {

    private static ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "na");
    private static ThreadLocal<Integer> level = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<String> status = ThreadLocal.withInitial(() -> "unknown");

    public static Integer getAndIncrease() {
        int currentLevel = level.get();
        level.set(currentLevel + 1);
        return currentLevel;
    }

    public static String getXID() {
        return threadLocal.get();
    }

    public static void setXID(String xid) {
        threadLocal.set(xid);
    }

    public static String getStatus() {
        return status.get();
    }

    public static void setStatus(String statusStr) {
        status.set(statusStr);
    }

    public static boolean isCommitted() {
        return "TxCommit".equals(getStatus());
    }

    public static void cleanUp(){
        threadLocal.set("na");
        level.set(0);
        status.set("unknown");
    }
}
