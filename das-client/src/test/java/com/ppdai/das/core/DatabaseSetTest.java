package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.ppdai.das.core.enums.DatabaseCategory.MYSQL_PROVIDER;

public class DatabaseSetTest {
    private DatabaseSet databaseSet;

    @Before
    public void before() throws Exception {
        databaseSet = new DatabaseSet("dbset", MYSQL_PROVIDER,
                ImmutableMap.of("db", new DataBase("db", true, "sh", "connectionString")));
    }

    @Test
    public void testDeepCopy() throws Exception {
        DatabaseSet databaseSet1 = databaseSet.deepCopy(
                ImmutableMap.of("db2", new DataBase("db2", true, "sh", "connectionString")));
        Assert.assertEquals("db2", databaseSet1.getDatabases().get("db2").getName());
        databaseSet1.remove("db2");
        Assert.assertTrue(databaseSet1.getDatabases().isEmpty());
        Assert.assertEquals(MYSQL_PROVIDER, databaseSet1.getProvider());
    }

    @Test
    public void testHash() throws Exception  {
        DatabaseSet databaseSet2 = new DatabaseSet("dbset", MYSQL_PROVIDER,
                ImmutableMap.of("db", new DataBase("db", true, "sh", "connectionString")));
        HashMap<DatabaseSet, String> map = new HashMap<>();
        map.put(databaseSet, "1");
        Assert.assertEquals("1", map.get(databaseSet2));
    }
}
