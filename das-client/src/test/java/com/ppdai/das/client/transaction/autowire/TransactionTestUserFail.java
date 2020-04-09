package com.ppdai.das.client.transaction.autowire;

import com.ppdai.das.client.transaction.normal.BaseTransactionAnnoClass;
import com.ppdai.das.client.transaction.normal.TransactionTestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TransactionTestUserFail implements TransactionTestUser {
    @Autowired
    private TransactionAnnoClass test;
    
    public String perform() {
        return test.perform();
    }
    
    public String performNest() {
        return test.perform();
    }

    @Override
    public BaseTransactionAnnoClass getTransactionAnnoTest() {
        return null;
    }
}
