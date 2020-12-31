package com.ppdai.das.tx.event;

public class CommitEvent {
    String name;

    public String getName() {
        return name;
    }

    public CommitEvent setName(String name) {
        this.name = name;
        return this;
    }
}
