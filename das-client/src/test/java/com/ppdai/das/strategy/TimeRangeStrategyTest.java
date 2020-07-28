package com.ppdai.das.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ppdai.das.client.Hints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class TimeRangeStrategyTest {
    @Test
    public void test(){
        TimeRangeStrategy timeRangeStrategy = new TimeRangeStrategy();
        timeRangeStrategy.initialize(ImmutableMap.<String,String>builder()
                .put("columns", "c1")
                .put("tableColumns","c2")
                .put("shardByDb", "true")
                .put("shardByTable", "true")
                .put("zeroPadding", "1")
                .put("tableZeroPadding", "1")
                .put("pattern", "DAY_OF_WEEK")
                .put("tablePattern", "DAY_OF_WEEK").build());
        Set<String> shards = timeRangeStrategy.locateDbShardsByValue(null, "2020-07-28 11:11:11");
        Assert.assertTrue(shards.contains("3"));

        Set<String> dbShards = timeRangeStrategy.locateDbShards(createContext("2020-07-28 11:11:11"));
        Assert.assertTrue(dbShards.contains("3"));

        Set<String> tShards = timeRangeStrategy.locateTableShardsByValue(null, "2020-07-28 11:11:11");
        Assert.assertTrue(tShards.contains("3"));

        TableShardingContext tableShardingContext = new TableShardingContext("appId", "logicDbName",  "tName",
                ImmutableSet.of("1", "2", "3"), Hints.hints(), ConditionList.andList());
        ColumnCondition columnCondition = new ColumnCondition(OperatorEnum.EQUAL, "tName", "c2", "2020-07-28 11:11:11");
        TableConditionContext tableConditionContext = new TableConditionContext(tableShardingContext, columnCondition);
        Set<String> tShards2 = timeRangeStrategy.locateTableShards(tableConditionContext);
        Assert.assertTrue(tShards2.contains("3"));
    }

    private ConditionContext createContext(Object value) {
        return new ShardingContext("appId", "logicDbName",
                Sets.newHashSet("shard"), new Hints(), ConditionList.andList())
                .create(new ColumnCondition(OperatorEnum.EQUAL, "tableName", "col", value));
    }
}
