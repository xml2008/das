package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.core.configure.DataSourceConfigureLocatorManager;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class KeyHolderTest {

    @Test
    public void test() throws SQLException {
        DasConfigureContext dasConfigureContext = new DasConfigureContext(new DefaultLogger());
        DasConfigureFactory.initialize(dasConfigureContext);
        KeyHolder keyHolder = new KeyHolder();
        keyHolder.initialize(1);
        keyHolder.addKey(ImmutableMap.of("1", Integer.parseInt("2")));
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        List<Number> ids = keyHolder.getIdList();
        Assert.assertEquals(1, keyList.size());
        Assert.assertEquals(1, ids.size());
        Assert.assertEquals(2, keyHolder.getKey());
        Assert.assertEquals(2, keyHolder.getKey(0));
        Assert.assertEquals(1, keyHolder.getKeys().size());
        Assert.assertEquals(1, keyHolder.getUniqueKey().size());
        Assert.assertFalse(keyHolder.isMerged());
        Assert.assertFalse(keyHolder.isRequireMerge());

        keyHolder.requireMerge();
        keyHolder.addKeys(Lists.newArrayList(ImmutableMap.of("3", Integer.parseInt("4"))));
        Assert.assertEquals(1, keyHolder.getUniqueKey().size());
    }

}
