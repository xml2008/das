package com.ppdai.das.client.transaction.javaConfig;

import com.ppdai.das.client.annotation.DasTransactional;
import com.ppdai.das.core.client.DalTransactionManager;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionAnnoClass {
    public static final String DB_NAME = "MySqlSimple";
    
    @Autowired
    private AnotherClass test;

    public AnotherClass getTest() {
        return test;
    }

    @DasTransactional(logicDbName = DB_NAME)
    public String perform() {
        Assert.assertTrue(DalTransactionManager.isInTransaction());
        return null;
    }
    
    @DasTransactional(logicDbName = DB_NAME)
    public String performOld() {
        Assert.assertTrue(DalTransactionManager.isInTransaction());
        return null;
    }

    @DasTransactional(logicDbName = DB_NAME)
    public boolean performPrivateInnner() {
        return performPrivate();
    }

    private boolean performPrivate() {
        Assert.assertTrue(DalTransactionManager.isInTransaction());
        return true;
    }
}
