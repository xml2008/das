package com.ppdai.das.strategy;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeRangeStrategyTest {
    TimeRangeStrategy shardStrategy;
    Date testDate;

    /*@Before
    public void before() throws ParseException {
        shardStrategy = new TimeRangeStrategy();
        testDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-04-28 08:06:05");
    }

    @Test
    public void testDay() {
        shardStrategy.initialize(ImmutableMap.of("select", "day"));
        String shard1 = shardStrategy.calculateDbShard(testDate);
        Assert.assertEquals("20200428", shard1);
        String shard2 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("20200428", shard2);

        java.sql.Date sqlDate = new java.sql.Date(testDate.getTime());
        String shard3 = shardStrategy.calculateDbShard(sqlDate);
        Assert.assertEquals("20200428", shard3);
        String shard4 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("20200428", shard4);
    }

    @Test
    public void testWeek() throws ParseException {
        shardStrategy.initialize(ImmutableMap.of("select", "week"));
        String shard1 = shardStrategy.calculateDbShard(testDate);
        Assert.assertEquals("20200405", shard1);
        String shard2 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("20200405", shard2);

        java.sql.Date sqlDate = new java.sql.Date(testDate.getTime());
        String shard3 = shardStrategy.calculateDbShard(sqlDate);
        Assert.assertEquals("20200405", shard3);
        String shard4 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("20200405", shard4);
    }

    @Test
    public void testMonth() throws ParseException {
        shardStrategy.initialize(ImmutableMap.of("select", "month"));
        String shard1 = shardStrategy.calculateDbShard(testDate);
        Assert.assertEquals("202004", shard1);
        String shard2 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("202004", shard2);

        java.sql.Date sqlDate = new java.sql.Date(testDate.getTime());
        String shard3 = shardStrategy.calculateDbShard(sqlDate);
        Assert.assertEquals("202004", shard3);
        String shard4 = shardStrategy.calculateTableShard("table", testDate);
        Assert.assertEquals("202004", shard4);
    }*/
}
