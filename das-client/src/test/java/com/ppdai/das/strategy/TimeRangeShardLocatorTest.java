package com.ppdai.das.strategy;

import com.google.common.collect.Sets;
import com.ppdai.das.client.Hints;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TimeRangeShardLocatorTest {
    Set<String> allShards;
    TimeRangeShardLocator locator;

    private Set<String> rangeSet(int min, int max) {
        Set<String> set = new HashSet<>();
        for(int i = min; i <= max; i++) {
            set.add(i + "");
        }
        return set;
    }

    private ConditionContext createContext(Object value) {
        return new ShardingContext("appId", "logicDbName",
                allShards, new Hints(), ConditionList.andList())
                .create(new ColumnCondition(OperatorEnum.EQUAL, "tableName", "col", value));
    }

    private ConditionContext createContext(Object value, Object value2) {
        return new ShardingContext("appId", "logicDbName",
                allShards, new Hints(), ConditionList.andList())
                .create(new ColumnCondition(OperatorEnum.EQUAL, "tableName", "col", value, value2));
    }

    @Test
    public void testDayOfYear() {
        allShards = rangeSet(1, 366);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.DAY_OF_YEAR);
        Set<String> s1 = locator.locateForEqual(createContext("2020-01-01 00:00:00"));
        assertEquals(Sets.newHashSet("1"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-31 00:00:00"));
        assertEquals(Sets.newHashSet("152"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-01 00:00:00", "2020-05-02 00:00:00"));
        assertEquals(rangeSet(122, 123), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-12-30 00:00:00", "2021-01-01 00:00:00"));
        assertEquals(Sets.newHashSet("365", "366", "1"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-1-1 00:00:00", "2021-01-02 00:00:00"));
        assertEquals(allShards, s7);
    }

    @Test
    public void testDayOfMonth(){
        allShards = rangeSet(1, 31);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.DAY_OF_MONTH);
        Set<String> s1 = locator.locateForEqual(createContext("2020-05-01 00:00:00"));
        assertEquals(Sets.newHashSet("1"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-31 00:00:00"));
        assertEquals(Sets.newHashSet("31"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s3);

        //Within a period
        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s4);

        //Cross a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-01 00:00:00", "2020-05-08 00:00:00"));
        assertEquals(rangeSet(1, 8), s5);

        //Include a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-04-30 00:00:00", "2020-05-01 00:00:00"));
        assertEquals(Sets.newHashSet("30", "1"), s6);
    }

    @Test
    public void testWeekOfYear() {
        allShards = rangeSet(1, 54);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.WEEK_OF_YEAR);

        Set<String> s1 = locator.locateForEqual(createContext("2020-01-01 00:00:00"));
        assertEquals(Sets.newHashSet("1"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-12-26 00:00:00"));
        assertEquals(Sets.newHashSet("52"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-01-01 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-01-01 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-01 00:00:00", "2020-05-02 00:00:00"));
        assertEquals(Sets.newHashSet("18"), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-12-26 00:00:00", "2021-01-01 00:00:00"));
        assertEquals(Sets.newHashSet("52", "1"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-01-01 00:00:00", "2021-01-01 00:00:00"));
        assertEquals(allShards, s7);
    }

    @Test
    public void testWeekOfMonth() {
        allShards = rangeSet(1, 6);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.WEEK_OF_MONTH);

        Set<String> s1 = locator.locateForEqual(createContext("2020-05-01 00:00:00"));
        assertEquals(Sets.newHashSet("1"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-31 00:00:00"));
        assertEquals(Sets.newHashSet("6"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-01 00:00:00", "2020-05-08 00:00:00"));
        assertEquals(rangeSet(1, 2), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-05-31 00:00:00", "2020-06-01 00:00:00"));
        assertEquals(Sets.newHashSet("6", "1"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-05-01 00:00:00", "2020-06-01 00:00:00"));
        assertEquals(allShards, s7);
    }

    @Test
    public void testDayOfWeek() {
        allShards = rangeSet(1, 7);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.DAY_OF_WEEK);
        Set<String> s1 = locator.locateForEqual(createContext("2020-05-17 00:00:00"));
        assertEquals(Sets.newHashSet("1"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-23 00:00:00"));
        assertEquals(Sets.newHashSet("7"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-18 00:00:00"));
        assertEquals(rangeSet(1, 2), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-05-23 00:00:00", "2020-05-24 00:00:00"));
        assertEquals(Sets.newHashSet("7", "1"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-05-12 00:00:00", "2020-05-19 00:00:00"));
        assertEquals(allShards, s7);
    }


    @Test
    public void testHourOfDay() {
        allShards = rangeSet(0, 23);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.HOUR_OF_DAY);
        
        Set<String> s1 = locator.locateForEqual(createContext("2020-05-17 00:00:00"));
        assertEquals(Sets.newHashSet("0"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-23 23:00:00"));
        assertEquals(Sets.newHashSet("23"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-31 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-17 01:00:00"));
        assertEquals(Sets.newHashSet("0", "1"), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-05-17 23:00:00", "2020-05-18 00:00:00"));
        assertEquals(Sets.newHashSet("23", "0"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-18 00:00:00"));
        assertEquals(allShards, s7);
    }

    @Test
    public void testMinuteOfHour() {
        allShards = rangeSet(0, 59);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.MINUTE_OF_HOUR);

        Set<String> s1 = locator.locateForEqual(createContext("2020-05-17 00:00:00"));
        assertEquals(Sets.newHashSet("0"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-17 00:59:00"));
        assertEquals(Sets.newHashSet("59"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-17 00:01:00"));
        assertEquals(Sets.newHashSet("0", "1"), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-05-17 00:59:00", "2020-05-17 01:00:00"));
        assertEquals(Sets.newHashSet("59", "0"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-17 10:00:00"));
        assertEquals(allShards, s7);
    }

    @Test
    public void testSecondOfMinute() {
        allShards = rangeSet(0, 59);
        locator = new TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN.SECOND_OF_MINUTE);
        Set<String> s1 = locator.locateForEqual(createContext("2020-05-17 00:00:00"));
        assertEquals(Sets.newHashSet("0"), s1);

        Set<String> s2 = locator.locateForEqual(createContext("2020-05-17 00:00:59"));
        assertEquals(Sets.newHashSet("59"), s2);

        Set<String> s3 = locator.locateForGreaterThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s3);

        Set<String> s4 = locator.locateForLessThan(createContext("2020-05-17 00:00:00"));
        assertEquals(allShards, s4);

        //Within a period
        Set<String> s5 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-17 00:00:01"));
        assertEquals(Sets.newHashSet("0", "1"), s5);

        //Cross a period
        Set<String> s6 = locator.locateForBetween(createContext("2020-05-17 00:00:59", "2020-05-17 00:01:00"));
        assertEquals(Sets.newHashSet("59", "0"), s6);

        //Include a period
        Set<String> s7 = locator.locateForBetween(createContext("2020-05-17 00:00:00", "2020-05-17 00:01:00"));
        assertEquals(allShards, s7);
    }
}
