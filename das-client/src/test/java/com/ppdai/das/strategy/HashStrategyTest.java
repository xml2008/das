package com.ppdai.das.strategy;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

public class HashStrategyTest {
    HashStrategy strategy;

    @Before
    public void before() throws ParseException {
        strategy = new HashStrategy();
    }

    @Test
    public void testMD5() {
        strategy.initialize(ImmutableMap.of("hash", "md5", "mod", "10"));
        Assert.assertEquals("2", strategy.calculateDbShard("azbc"));
    }

    @Test
    public void testCRC32() {
        strategy.initialize(ImmutableMap.of("hash", "crc32", "mod", "10"));
        Assert.assertEquals("9", strategy.calculateDbShard("azbc"));
    }
}
