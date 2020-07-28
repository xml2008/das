package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.ppdai.das.core.configure.ConnectionString;
import com.ppdai.das.core.configure.DataSourceConfigure;
import com.ppdai.das.core.configure.DataSourceConfigureLocatorManager;
import org.junit.Assert;
import org.junit.Test;

import static com.ppdai.das.core.enums.IPDomainStatus.IP;
import static com.ppdai.das.core.helper.Ordered.LOWEST_PRECEDENCE;


public class DefaultDataSourceConfigureLocatorTest {
    @Test
    public void test(){
        ConnectionString connectionString = new ConnectionString("name",  "Data Source=jdbc:mysql://localhost:3306/dal_shard_1;UID=sa;password=sa;", "domainConnectionString");
        DefaultDataSourceConfigureLocator locator = (DefaultDataSourceConfigureLocator) DataSourceConfigureLocatorManager.getInstance();
        DataSourceConfigure dataSourceConfigure = new DataSourceConfigure();
        dataSourceConfigure.setProperty("k", "v");
        locator.setPoolProperties(dataSourceConfigure);
        DataSourceConfigure dataSourceConfigure1 = locator.mergeDataSourceConfigure(connectionString);
        Assert.assertEquals("sa", dataSourceConfigure1.getUserName());
        Assert.assertEquals(LOWEST_PRECEDENCE, locator.getOrder());
        Assert.assertEquals(IP, locator.getIPDomainStatus());

        locator.addDataSourceConfigureKeySet(Sets.newHashSet("k", "v"));
        locator.setConnectionStrings(ImmutableMap.of("k", connectionString));
        Assert.assertEquals(2, locator.getDataSourceConfigureKeySet().size());
    }
}
