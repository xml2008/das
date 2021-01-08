package com.ppdai.das.tx.event;

public class BusEvent {
    String message = "";

    public BusEvent(String msg) {
        message = msg;
    }

    @Override
    public String toString() {
        return "BusEvent{" +
                "message='" + message + '\'' +
                '}';
    }
}
