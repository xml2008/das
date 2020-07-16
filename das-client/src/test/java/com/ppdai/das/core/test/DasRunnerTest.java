package com.ppdai.das.core.test;

import com.ppdai.das.client.DasClient;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Person;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.client.transaction.DasTransactionalEnabler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DasTransactionalEnabler.class, AnnotationAwareAspectJAutoProxyCreator.class,
        DasRunnerTest.class, DaoBean.class})
@SpringBootTest
public class DasRunnerTest  {

    static final String DB_NAME = "MySqlSimple";
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
    public void testRollback() throws SQLException {
        isRollback = true;
        Person p = new Person();
        p.setName("name");
        daoBean.testRollback(p);
    }

    @Test
    public void testCommit() throws SQLException {
        isRollback = false;
        Person p = new Person();
        p.setName("name");
        daoBean.testCommit(p);
    }

    @Test
    public void testNestedTransaction() throws SQLException {
        isRollback = true;
        daoBean.rollbackAndCommit();
    }

    @Test(expected = SQLException.class)
    public void testNestedTransactionException() throws SQLException {
        isRollback = true;
        daoBean.commitAndRollback();
    }
}
