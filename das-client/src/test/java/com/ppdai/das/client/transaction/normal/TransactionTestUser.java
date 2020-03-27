package com.ppdai.das.client.transaction.normal;


public interface TransactionTestUser {
    String perform();

    String performNest();
    
    BaseTransactionAnnoClass getTransactionAnnoTest();
}
