package com.ppdai.das.client.transaction.beanDefine;


import com.ppdai.das.client.transaction.DasTransactional;
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
}
