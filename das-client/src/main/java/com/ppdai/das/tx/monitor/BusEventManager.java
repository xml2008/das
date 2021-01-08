package com.ppdai.das.tx.monitor;

import com.google.common.eventbus.EventBus;

public class BusEventManager {

    private static EventBus eventBus = new EventBus();


    public static void register(Object obj) {
        eventBus.register(obj);
    }

    public static void post(Object event) {
        eventBus.post(event);
    }
}
