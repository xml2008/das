package com.ppdai.das.client.transaction.autowire;


import com.ppdai.das.client.transaction.annotation.DasTransactional;
import org.springframework.stereotype.Component;


@Component
public class TransactionAnnoClass {
    public static final String DB_NAME = "MySqlSimple";
    
    @DasTransactional(logicDbName = DB_NAME)
    public String perform() {
        return null;
    }
}
