package com.ppdai.das.strategy;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ppdai.das.client.Hints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class HashCRCModShardLocatorTest {

    private ConditionContext contextEq =  new ShardingContext("appId", "logicDbName", ImmutableSet.of(), new Hints(), ConditionList.andList())
            .create(new ColumnCondition(OperatorEnum.EQUAL, "tableName", "col", "abc"));

    private ConditionContext contextBetween =  new ShardingContext("appId", "logicDbName", ImmutableSet.of("all"), new Hints(), ConditionList.andList())
            .create(new ColumnCondition(OperatorEnum.BEWTEEN, "tableName", "col","abc", "xyz"));

    private ConditionContext contextInMD5 = new ShardingContext("appId", "logicDbName", ImmutableSet.of("00", "04"), new Hints(), ConditionList.andList())
            .create(new ColumnCondition(OperatorEnum.IN, "tableName", "col", Lists.newArrayList("abc", "xyz")));

    private ConditionContext contextInCRC = new ShardingContext("appId", "logicDbName", ImmutableSet.of("01", "08"), new Hints(), ConditionList.andList())
            .create(new ColumnCondition(OperatorEnum.IN, "tableName", "col", Lists.newArrayList("abc", "xyz")));

    @Test
    public void testMD5Eq() {
        HashModShardLocator locator = new HashModShardLocator(10, "%02d");
        //"abc" md5 value: HEX: 900150983CD24FB0D6963F7D28E17F72 (191415658344158766168031473277922803570)
        Set<String> s = locator.locateForEqual(contextEq);
        Assert.assertEquals(ImmutableSet.of("00"), s);
    }

    @Test
    public void testMD5Between() {
        HashModShardLocator locator = new HashModShardLocator(10, "%02d");
        Set<String> s = locator.locateForBetween(contextBetween);
        Assert.assertEquals(ImmutableSet.of("all"), s);
    }

    @Test
    public void testMD5In() {
        HashModShardLocator locator = new HashModShardLocator(10, "%02d");
        Set<String> s = locator.locateForIn(contextInMD5);
        Assert.assertEquals(ImmutableSet.of("00", "04"), s);
    }

    @Test
    public void testCRCEq() {
        CRCModShardLocator locator = new CRCModShardLocator(10, "%02d");

        //"abc" CRC value: 891568578
        Set<String> s = locator.locateForEqual(contextEq);
        Assert.assertEquals(ImmutableSet.of("08"), s);
    }

    @Test
    public void testCRCBetween() {
        CRCModShardLocator locator = new CRCModShardLocator(10, "%02d");
        Set<String> s = locator.locateForBetween(contextBetween);
        Assert.assertEquals(ImmutableSet.of("all"), s);
    }

    @Test
    public void testCRCIn() {
        CRCModShardLocator locator = new CRCModShardLocator(10, "%02d");
        Set<String> s = locator.locateForIn(contextInCRC);
        Assert.assertEquals(ImmutableSet.of("01", "08"), s);
    }
}
