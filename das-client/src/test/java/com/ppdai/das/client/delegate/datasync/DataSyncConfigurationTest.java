package com.ppdai.das.client.delegate.datasync;

import com.ppdai.das.client.Hints;
import com.ppdai.das.service.DasOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSyncConfigurationTest {

    @Test
    public void testDataSyncConfiguration() {
        DataSyncConfiguration.getInstance().disableSyncMode();
        assertFalse(DataSyncConfiguration.getInstance().isEnableSyncMode());
        DataSyncConfiguration.getInstance().enableSyncMode();

        DasDataSynchronizer dasDataSynchronizer1 = DataSyncConfiguration.getInstance().getDasDataSynchronizer("dbA");
        assertEquals("TestSynchronizer", dasDataSynchronizer1.getClass().getSimpleName());

        DasDataSynchronizer dasDataSynchronizer2 = DataSyncConfiguration.getInstance().getDasDataSynchronizer("dbB");
        assertEquals("TestSynchronizer", dasDataSynchronizer2.getClass().getSimpleName());
        assertTrue(DataSyncConfiguration.getInstance().isEnableSyncMode());
    }

    @Test
    public void testDataSyncConfigurationJob() throws InterruptedException {
        DataSyncConfiguration.getInstance().enableSyncMode();

        TestSynchronizer dasDataSynchronizer1 = (TestSynchronizer)DataSyncConfiguration.getInstance().getDasDataSynchronizer("dbA");
        assertEquals("TestSynchronizer", dasDataSynchronizer1.getClass().getSimpleName());
        TimeUnit.SECONDS.sleep(2);

        assertTrue(dasDataSynchronizer1.jobCount > 0);
        DataSyncConfiguration.getInstance().skipValidateScheduler();
        int t1 = dasDataSynchronizer1.jobCount;
        TimeUnit.SECONDS.sleep(2);
        assertEquals(t1, dasDataSynchronizer1.jobCount);

        DataSyncConfiguration.getInstance().continueValidateScheduler();
        TimeUnit.SECONDS.sleep(2);
        assertTrue(dasDataSynchronizer1.jobCount > t1);
    }

    @Test
    public void testDataSyncConfigurationQueue() throws InterruptedException {
        DataSyncConfiguration configuration = DataSyncConfiguration.getInstance();
        configuration.enableSyncMode();

        TestSynchronizer dasDataSynchronizer1 = (TestSynchronizer)configuration.getDasDataSynchronizer("dbA");

        configuration.sendContext(new DataSyncContext("dbA").setData("CA"));
        configuration.sendContext(new DataSyncContext("dbB").setData("CA"));
        TimeUnit.SECONDS.sleep(1);
        assertEquals(2, dasDataSynchronizer1.syncCount);
    }

    @Test
    public void testDataSyncContext() {
        DataSyncContext context = new DataSyncContext("logicDbName");
        Date date = new Date();
        context.setData("d");
        context.setHints(Hints.hints());
        context.setResult("r");
        context.setException(new Exception("test"));
        context.setInTransaction(true);
        context.setSequenceId(1L);
        context.setGlobalSequenceId(2L);
        context.setSinceTime(date);
        context.setTimestamp(date);
        context.setDasOperation(DasOperation.Insert);
        context.setLogicDbName("l");
        context.setIp("i");
        Assert.assertEquals("d", context.getData());
        Assert.assertEquals("r", context.getResult());
        Assert.assertEquals("test", context.getException().getMessage());
        Assert.assertTrue(context.isInTransaction());
        Assert.assertEquals(1L, context.getSequenceId());
        Assert.assertEquals(2L, context.getGlobalSequenceId());
        Assert.assertEquals(date, context.getSinceTime());
        Assert.assertEquals(date, context.getTimestamp());
        Assert.assertEquals(DasOperation.Insert, context.getDasOperation());
        Assert.assertEquals("l", context.getLogicDbName());
        Assert.assertNotNull(context.getHints());
        Assert.assertEquals("i", context.getIp());
    }
}
