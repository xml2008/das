package com.ppdai.das.core.test;

import com.ppdai.das.client.DasClient;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Person;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.client.annotation.DasTransactional;
import com.ppdai.das.client.transaction.DasTransactionalEnabler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;

@RunWith(DasRunner.class)
@ContextConfiguration(classes = {DasTransactionalEnabler.class, DasRunnerTest.class, DaoBean.class})
@SpringBootTest
public class DasRunnerTest {

    private static final String DB_NAME = "MySqlSimple";
    private DasClient dasClient;
    private boolean isRollback = false;
    private long count = 0;

    @Autowired
    DaoBean daoBean;

    @Before
    public void before() throws SQLException {
        dasClient = DasClientFactory.getClient(DB_NAME);
        count = dasClient.queryObject(SqlBuilder.selectCount().from(Person.PERSON).intoObject());
    }

    @After
    public void after() throws SQLException {
        long now = dasClient.queryObject(SqlBuilder.selectCount().from(Person.PERSON).intoObject());
        Assert.assertEquals(now, isRollback ? count : count + 1);
    }

    @Test
    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void testRollback() throws SQLException {
        isRollback = true;
        Person p = new Person();
        p.setName("name");
        dasClient.insert(p);
    }

    @Test
    @DasTransactional(logicDbName = DB_NAME)
    public void testCommit() throws SQLException {
        isRollback = false;
        Person p = new Person();
        p.setName("name");
        dasClient.insert(p);
    }

    @Test
    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void testNestedTransaction() throws SQLException {
        isRollback = true;
        daoBean.commitMethod();
    }

    @Test(expected = SQLException.class)
    @DasTransactional(logicDbName = DB_NAME, rollback = false)
    public void testNestedTransactionException() throws SQLException {
        isRollback = true;
        daoBean.rollbackMethod();
    }
}
