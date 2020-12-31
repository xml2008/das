package com.ppdai.das.tx;

import java.util.HashMap;
import java.util.Map;

public class DasTxContext {

    private static ThreadLocal<XID> threadLocal = ThreadLocal.withInitial(() -> new XID());

    public static XID getXID() {
        return threadLocal.get();
    }

    public static void setXID(XID xid) {
        threadLocal.set(xid);
    }
}
