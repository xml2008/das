package com.ppdai.das.strategy;

import java.util.Map;

public class DivisorStrategy extends ShardColModShardStrategy {
    public static final String DIVISOR = "divisor";
    private Integer divisor;

    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);
        if (settings.containsKey(DIVISOR)) {
            divisor = Integer.parseInt(settings.get(DIVISOR));
        } else {
            throw new RuntimeException("please configure 'divisor' number for DivisorStrategy");
        }
    }

    @Override
    public String calculateDbShard(Object value) {
        Long id = getLongValue(value);
        return String.format("%02d", (int) ((id % getMod()) / divisor) + 1);
    }

    @Override
    public String calculateTableShard(String tableName, Object value) {
        Long id = getLongValue(value);
        return String.format("%02d", id % getTableMod() + 1);
    }
}
