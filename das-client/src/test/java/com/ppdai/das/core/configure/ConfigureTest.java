package com.ppdai.das.core.configure;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public class ConfigureTest {
    @Test
    public void testParser(){
        ConnectionStringParser parser = ConnectionStringParser.getInstance();
        ConnectionStringConfigure configure = parser.parse("name", "Data Source=jdbc:mysql://localhost:3306/dal_shard_1;UID=sa;password=sa;");
        Assert.assertEquals("name", configure.getName());
        Assert.assertEquals("sa", configure.getPassword());
    }

    @Test
    public void testPropertyFileConfigureProvider() throws Exception {
        DataSourceConfigure configure = createDataSourceConfigure();
        Assert.assertEquals("root", configure.getUserName());
        Assert.assertEquals("root", configure.getPassword());
    }

    @Test
    public void testDataSourceConfigureWrapper() throws Exception {
        DataSourceConfigure configure = createDataSourceConfigure();
        DataSourceConfigureWrapper wrapper = new DataSourceConfigureWrapper(ImmutableMap.of(), configure, ImmutableMap.of());
        wrapper.setOriginalMap(ImmutableMap.of());
        wrapper.setDataSourceConfigure(configure);
        wrapper.setDataSourceConfigureMap(ImmutableMap.of());
        Assert.assertEquals(ImmutableMap.of(), wrapper.getOriginalMap());
        Assert.assertEquals(ImmutableMap.of(), wrapper.getDataSourceConfigureMap());
        Assert.assertEquals("ds1", wrapper.getDataSourceConfigure().getName());
    }

    private DataSourceConfigure createDataSourceConfigure() throws Exception {
        PropertyFileConfigureProvider provider = new PropertyFileConfigureProvider();
        provider.initialize(ImmutableMap.of());
        provider.setup(ImmutableSet.of("ds1"));
        return provider.getDataSourceConfigure("ds1");
    }
}
