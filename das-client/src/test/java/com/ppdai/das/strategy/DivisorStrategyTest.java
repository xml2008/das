package com.ppdai.das.strategy;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class DivisorStrategyTest {
    DivisorStrategy shardStrategy;

    @Before
    public void before() {
        shardStrategy = new DivisorStrategy();
        shardStrategy.initialize(ImmutableMap.of("divisor", "2", "mod", "10", "tableMod", "10"));
    }

    @Test
    public void test() {
        String shard = shardStrategy.calculateDbShard(5);
        String tableShard = shardStrategy.calculateTableShard("table",5);
        Assert.assertEquals("03", shard);
        Assert.assertEquals("06", tableShard);
    }
}