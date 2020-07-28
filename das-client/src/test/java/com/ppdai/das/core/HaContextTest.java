package com.ppdai.das.core;

import com.ppdai.das.core.enums.DatabaseCategory;
import com.ppdai.das.core.status.StatusManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

public class HaContextTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        StatusManager.initializeGlobal();
    }

    @Test
    public void test(){
        HaContext context = new HaContext();
        context.update(new SQLException("test", "1043", 1043));
        Assert.assertTrue(context.getException() instanceof SQLException);
        Assert.assertTrue(context.isRetry());
        Assert.assertEquals(1, context.getRetryCount());
        context.clear();
        Assert.assertFalse(context.isRetry());

        Assert.assertNull(context.getDB());
        context.addDB("db");
        Assert.assertTrue(context.contains("db"));
        Assert.assertEquals("db", context.getDB());

        Assert.assertFalse(context.needTryAgain());
        context.setOver(true);
        Assert.assertTrue(context.isOver());
        context.setDatabaseCategory(DatabaseCategory.MySql);
        Assert.assertEquals(DatabaseCategory.MySql, context.getDatabaseCategory());
    }
}
