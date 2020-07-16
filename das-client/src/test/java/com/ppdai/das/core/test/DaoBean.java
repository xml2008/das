package com.ppdai.das.core.test;

import com.ppdai.das.client.DasClient;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Person;
import com.ppdai.das.client.annotation.DasTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import static com.ppdai.das.core.test.DasRunnerTest.DB_NAME;

@Component
public class DaoBean {

    @Autowired
    DaoBean2 daoBean2;

    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void rollbackAndCommit() throws SQLException {
        daoBean2.commit();
    }

    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void rollback(Person p) throws SQLException {
        DasClient dasClient = DasClientFactory.getClient(DB_NAME);
        dasClient.insert(p);
    }

    @DasTransactional(logicDbName = DB_NAME)
    public void commit(Person p) throws SQLException {
        DasClient dasClient = DasClientFactory.getClient(DB_NAME);
        dasClient.insert(p);
    }

    @DasTransactional(logicDbName = DB_NAME, rollback = false)
    public void commitAndRollback() throws SQLException {
        daoBean2.rollback();
    }

}
