package com.ppdai.das.core.configure;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Properties;

public class DataSourceConfigureTest {
    @Test
    public void test(){
        DataSourceConfigure dataSourceConfigure = new DataSourceConfigure("name", ImmutableMap.of("k", "v"));
        Properties properties = new Properties();
        properties.setProperty("k1", "v1");
        dataSourceConfigure.setProperties(properties);
        Properties p1 = dataSourceConfigure.toProperties();
        Assert.assertEquals("v1", p1.getProperty("k1"));

        String url = dataSourceConfigure.toConnectionUrl();
        Assert.assertEquals("{ConnectionUrl:null,Version:,CRC:}", url);
        Assert.assertFalse(dataSourceConfigure.dynamicPoolPropertiesEnabled());

        HashMap<DataSourceConfigure, String> map = new HashMap<>();
        map.put(dataSourceConfigure, "1");

        DataSourceConfigure dataSourceConfigure2 = new DataSourceConfigure("name", ImmutableMap.of("k", "v"));
        Assert.assertEquals("1", map.get(dataSourceConfigure));
        Assert.assertNotEquals(dataSourceConfigure2, dataSourceConfigure);
    }
}
