package com.ppdai.das.core.markdown;

import com.google.common.collect.ImmutableMap;
import com.ppdai.das.core.DasConfigure;
import com.ppdai.das.core.DasConfigureContext;
import com.ppdai.das.core.DasConfigureFactory;
import com.ppdai.das.core.DataBase;
import com.ppdai.das.core.DatabaseSet;
import com.ppdai.das.core.DefaultConnectionLocator;
import com.ppdai.das.core.DefaultDatabaseSelector;
import com.ppdai.das.core.DefaultLogger;
import com.ppdai.das.core.enums.DatabaseCategory;
import com.ppdai.das.core.status.StatusManager;
import com.ppdai.das.core.status.TimeoutMarkdown;
import com.ppdai.das.core.task.DefaultTaskFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.SQLException;


public class MarkdownTest {

    @BeforeClass
    public static void beforeClass() throws Exception{
        DasConfigureContext dasConfigureContext = new DasConfigureContext(new DefaultLogger());
        DasConfigureFactory.initialize(dasConfigureContext);
        StatusManager.initializeGlobal();
        DasConfigure dasConfigure = new DasConfigure(
                "appid",
                ImmutableMap.of("databaseSetId", new DatabaseSet("databaseSetId", DatabaseCategory.MYSQL_PROVIDER, ImmutableMap.of("dbId", new DataBase("dbId",
                        true,
                        "sharding",
                        "connectionString")))),
                new DefaultLogger(),
                new DefaultConnectionLocator(),
                new DefaultTaskFactory(),
                new DefaultDatabaseSelector());
        StatusManager.registerApplication("appid", dasConfigure);
        MarkdownManager.init();
    }

    @After
    public void after(){
        MarkdownManager.shutdown();
    }

    @Test
    public void testAutoMarkdown(){
        MarkDownInfo info = new MarkDownInfo("connectionString","version", MarkDownPolicy.TIMEOUT, 10L);
        info.setReason(MarkDownReason.ERRORCOUNT);
        info.setTotal(1);
        info.setFail(1L);
        info.setStatus("status");
        MarkdownManager.autoMarkdown(info);
        Assert.assertFalse(MarkdownManager.isMarkdown("connectionString"));
    }

    @Test
    public void testAutoMarkup(){
        MarkupInfo info = new MarkupInfo("connectionString","version", 2);
        MarkdownManager.autoMarkup(info);
        Assert.assertFalse(MarkdownManager.isMarkdown("connectionString"));
    }

    @Test
    public void testResetAutoMarkdowns(){
        MarkdownManager.resetAutoMarkdowns();
        Assert.assertFalse(MarkdownManager.isMarkdown("connectionString"));
    }

    @Test
    public void testMarkupPhase(){
        MarkupPhase markupPhase = new MarkupPhase(5);
        markupPhase.isQualified();
        Assert.assertEquals(1, markupPhase.getTotal());
    }

    @Test
    public void testTimeBucketCounter() throws InterruptedException {
        TimeBucketCounter counter = new TimeBucketCounter(100, 1);
        counter.increase();
        Assert.assertEquals(1, counter.getCount());
    }

    @Test
    public void testTimeoutDetector() {
        TimeoutDetector detector = new TimeoutDetector();
        ErrorContext context = new ErrorContext("ec", DatabaseCategory.MySql, 1L, new SQLException("test"));
        TimeoutMarkdown tmb = StatusManager.getTimeoutMarkdown();
        tmb.setEnabled(true);
        detector.detect(context);
    }
}
