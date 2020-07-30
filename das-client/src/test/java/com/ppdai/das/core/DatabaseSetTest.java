package com.ppdai.das.core;

import com.google.common.collect.ImmutableMap;
import com.ppdai.das.core.markdown.ErrorContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;

import static com.ppdai.das.core.enums.DatabaseCategory.MYSQL_PROVIDER;
import static com.ppdai.das.core.enums.DatabaseCategory.MySql;
import static com.ppdai.das.core.enums.DatabaseCategory.Oracle;
import static com.ppdai.das.core.enums.DatabaseCategory.SqlServer;

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

    @Test
    public void testDatabaseCategory() {
        Assert.assertEquals("SELECT columns FROM effectiveTableName WHERE whereExp", MySql.buildList("effectiveTableName", "columns", "whereExp"));
        Assert.assertEquals("SELECT columns FROM effectiveTableName WHERE whereExp LIMIT 1", MySql.buildTop("effectiveTableName", "columns", "whereExp", 1));
        Assert.assertEquals("SELECT columns FROM effectiveTableName WHERE whereExp LIMIT 1, 2", MySql.buildPage("effectiveTableName", "columns", "whereExp", 1, 2));
        Assert.assertEquals("selectSqlTemplate limit 1, 2", MySql.buildPage("selectSqlTemplate", 1, 2));

        Assert.assertEquals("SELECT columns FROM effectiveTableName WITH (NOLOCK) WHERE whereExp", SqlServer.buildList("effectiveTableName", "columns", "whereExp"));
        Assert.assertEquals("SELECT TOP 1 columns FROM effectiveTableName WITH (NOLOCK) WHERE whereExp", SqlServer.buildTop("effectiveTableName", "columns", "whereExp", 1));
        Assert.assertEquals("SELECT columns FROM effectiveTableName WITH (NOLOCK) WHERE whereExp OFFSET 1 ROWS FETCH NEXT 2 ROWS ONLY", SqlServer.buildPage("effectiveTableName", "columns", "whereExp", 1, 2));
        Assert.assertEquals("selectSqlTemplate OFFSET 1 ROWS FETCH NEXT 2 ROWS ONLY", SqlServer.buildPage("selectSqlTemplate", 1, 2));
        SqlServer.isTimeOutException(new ErrorContext("name", SqlServer, 1, new SQLException("test")));

        Assert.assertEquals("SELECT columns FROM effectiveTableName WHERE whereExp", Oracle.buildList("effectiveTableName", "columns", "whereExp"));
        Assert.assertEquals("SELECT * FROM (SELECT columns FROM effectiveTableName WHERE whereExp) WHERE ROWNUM <= 1", Oracle.buildTop("effectiveTableName", "columns", "whereExp", 1));
        Assert.assertEquals("SELECT * FROM (SELECT ROWNUM RN, T1.* FROM (SELECT columns FROM effectiveTableName WHERE whereExp)T1 WHERE ROWNUM <= 3)T2 WHERE T2.RN >=1", Oracle.buildPage("effectiveTableName", "columns", "whereExp", 1, 2));
        Assert.assertEquals("SELECT * FROM (SELECT ROWNUM RN, T1.* FROM (selectSqlTemplate)T1 WHERE ROWNUM <= 3)T2 WHERE T2.RN >=1", Oracle.buildPage("selectSqlTemplate", 1, 2));
        Assert.assertEquals("q", Oracle.quote("q"));
    }
}
