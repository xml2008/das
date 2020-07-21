package com.ppdai.das.core.task;

public enum TaskType {
    QUERY,
    BATCH_QUERY,

    INSERT,
    BATCH_INSERT,
    COMBINED_INSERT,

    UPDATE,
    BATCH_UPDATE,

    DELETE,
    BATCH_DELETE,

    CALL,
    BATCH_CALL
}
