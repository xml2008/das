package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;

public class ResultMergerTest {
    @Test
    public void testSum() throws SQLException {
        test(new ResultMerger.LongSummary(), "shard", 1L, 1L);
        test(new ResultMerger.LongNumberSummary(), "shard", 1L, 1L);
        test(new ResultMerger.IntSummary(), "shard", 1, 1);
        test(new ResultMerger.DoubleSummary(), "shard", 1d, 1d);
        test(new ResultMerger.BigIntegerSummary(), "shard", BigInteger.valueOf(1), BigInteger.valueOf(1));
        test(new ResultMerger.BigDecimalSummary(), "shard", BigDecimal.valueOf(1), BigDecimal.valueOf(1));
    };

    private void test(ResultMerger resultMerger, String shard, Number partial, Number sum) throws SQLException {
        resultMerger.addPartial(shard, partial);
        Assert.assertEquals(sum, resultMerger.merge());
    }

    @Test
    public void testAvg() throws SQLException {
        testAvg(new ResultMerger.IntAverage(),  ImmutableMap.of("count", 1, "sum", 1), 1);
        testAvg(new ResultMerger.LongAverage(),  ImmutableMap.of("count", 1L, "sum", 1L), 1);
        testAvg(new ResultMerger.DoubleAverage(),  ImmutableMap.of("count", 1d, "sum", 1d), 1);
        testAvg(new ResultMerger.BigIntegerAverage(),  ImmutableMap.of("count", BigInteger.valueOf(1), "sum", BigInteger.valueOf(1)), 1);
        testAvg(new ResultMerger.BigDecimalAverage(),  ImmutableMap.of("count", BigDecimal.valueOf(1), "sum", BigDecimal.valueOf(1)), 1);
    }

    public <T, R> void testAvg(ResultMerger<Map<T, R>> resultMerger,  Map<String, Number> partial, Number sum) throws SQLException {
        resultMerger.addPartial("shard", (Map<T, R>) partial);
        Assert.assertEquals(sum, resultMerger.merge().get("count"));
    }
}
