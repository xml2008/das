package com.ppdai.das.core.test;

import com.ppdai.das.client.DasClient;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Person;
import com.ppdai.das.client.annotation.DasTransactional;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import static com.ppdai.das.core.test.DasRunnerTest.DB_NAME;

@Component
public class DaoBean2 {

    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void rollback() throws SQLException {
        insert();
    }

    @DasTransactional(logicDbName = DB_NAME, rollback = false)
    public void commit() throws SQLException {
        insert();
    }

    private void insert() throws SQLException {
        DasClient dasClient = DasClientFactory.getClient(DB_NAME);
        Person p = new Person();
        p.setName("name");
        dasClient.insert(p);
    }
}
