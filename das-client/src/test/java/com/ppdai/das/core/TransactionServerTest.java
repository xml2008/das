package com.ppdai.das.core;

import com.ppdai.das.client.Hints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class TransactionServerTest {
    private TransactionServer transactionServer;

    @Before
    public void before() throws SQLException{
         transactionServer = new TransactionServer("localhost", "123");
    }

    @Test
    public void testCommit() throws Exception {
        TransactionId transactionId = transactionServer.start("das-test", "MySqlSimple", Hints.hints());
        Assert.assertEquals(1, transactionServer.getCurrentCount());
        String r = transactionServer.doInTransaction(transactionId.getUniqueId(),()->"OK");
        Assert.assertEquals("OK", r);
        transactionServer.commit(transactionId.getUniqueId());
        Assert.assertNotNull(transactionId);
    }


    @Test
    public void testRollback() throws SQLException {
        TransactionId transactionId = transactionServer.start("das-test", "MySqlSimple", Hints.hints());
        Assert.assertEquals(1, transactionServer.getCurrentCount());
        transactionServer.rollback(transactionId.getUniqueId());
        Assert.assertNotNull(transactionId);
    }
}
