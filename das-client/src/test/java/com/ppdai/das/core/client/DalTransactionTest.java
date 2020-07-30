package com.ppdai.das.core.client;

import com.ppdai.das.core.DasConfigureContext;
import com.ppdai.das.core.DasConfigureFactory;
import com.ppdai.das.core.DefaultLogger;
import com.ppdai.das.core.enums.DatabaseCategory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.mockito.Mockito.*;

public class DalTransactionTest {
    @BeforeClass
    public static void beforeClass() {
        DasConfigureContext dasConfigureContext = new DasConfigureContext(new DefaultLogger());
        DasConfigureFactory.initialize(dasConfigureContext);
    }

    @Test
    public void testCommit() throws SQLException {
        Connection connection = mock(Connection.class);
        DalTransactionListener listener = mock(DalTransactionListener.class);
        DalConnection dalConnection = new DalConnection(connection, true, "sid",
                DbMeta.createIfAbsent("rdb", DatabaseCategory.MySql, connection));
        DalTransaction dalTransaction = new DalTransaction(dalConnection, "db", 1L);
        dalTransaction.register(listener);
        int level = dalTransaction.startTransaction();
        Assert.assertEquals(1, dalTransaction.getLevel());
        int level2 = dalTransaction.startTransaction();
        dalTransaction.endTransaction(level2);
        dalTransaction.endTransaction(level);
        Assert.assertEquals(1, level2);
        Assert.assertEquals(1, dalTransaction.getListeners().size());
        Assert.assertEquals(1, dalTransaction.getTimeout());
        Assert.assertTrue(dalTransaction.getTimeoutTime() >= new Date().getTime());
    }

    @Test
    public void testRollback() throws SQLException {
        Connection connection = mock(Connection.class);
        DalTransactionListener listener = mock(DalTransactionListener.class);
        DalConnection dalConnection = new DalConnection(connection, true, "sid",
                DbMeta.createIfAbsent("rdb", DatabaseCategory.MySql, connection));
        DalTransaction dalTransaction = new DalTransaction(dalConnection, "db", 1L);
        dalTransaction.register(listener);
        int level = dalTransaction.startTransaction();
        dalTransaction.rollbackTransaction();
        Assert.assertTrue(dalTransaction.isRolledBack());
        Assert.assertEquals(0, level);

        dalTransaction.setDefaultShardApplied(true);
        Assert.assertTrue(dalTransaction.isDefaultShardApplied());
    }
}
