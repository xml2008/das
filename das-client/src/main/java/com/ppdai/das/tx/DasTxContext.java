package com.ppdai.das.tx;

public class DasTxContext {

    private static ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "na");
    private static ThreadLocal<Integer> level = ThreadLocal.withInitial(() -> 0);

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

    public static void cleanUp(){
        threadLocal.set("na");
        level.set(0);
    }
}
