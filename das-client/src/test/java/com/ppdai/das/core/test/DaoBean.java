package com.ppdai.das.core.test;

import com.ppdai.das.client.DasClient;
import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Person;
import com.ppdai.das.client.annotation.DasTransactional;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class DaoBean {
    private static final String DB_NAME = "MySqlSimple";

    @DasTransactional(logicDbName = DB_NAME)
    public void commitMethod() throws SQLException {
        insert();
    }

    @DasTransactional(logicDbName = DB_NAME, rollback = true)
    public void rollbackMethod() throws SQLException {
        insert();
    }

    private void insert() throws SQLException {
        DasClient dasClient = DasClientFactory.getClient(DB_NAME);
        Person p = new Person();
        p.setName("name");
        dasClient.insert(p);
    }
}
