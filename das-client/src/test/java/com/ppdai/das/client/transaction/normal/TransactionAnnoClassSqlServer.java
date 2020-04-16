package com.ppdai.das.client.transaction.normal;


import com.ppdai.das.client.Hints;
import com.ppdai.das.client.annotation.DasTransactional;
import com.ppdai.das.client.annotation.Shard;


import java.sql.SQLException;

public class TransactionAnnoClassSqlServer extends BaseTransactionAnnoClass {
    public static final String DB_NAME = "SqlSvrSimple";
    public static final String DB_NAME_SHARD = "SqlSvrConditionDbShard";
    
    public TransactionAnnoClassSqlServer() {
        super(DB_NAME, DB_NAME_SHARD, "select 1");
    }
    @DasTransactional(logicDbName = DB_NAME)
    public String perform() {
        return super.perform();
    }

    @DasTransactional(logicDbName = DB_NAME)
    public String performFail() {
        return super.performFail();
    }

    @DasTransactional(logicDbName = DB_NAME)
    public String performNest() {
        return super.performNest();
    }

    public String performNest2() {
        return super.performNest2();
    }


    public String performNest3() throws InstantiationException, IllegalAccessException {
        return super.performNest3();
    }

    @DasTransactional(logicDbName = DB_NAME)
    public String performNestDistributedTransaction() {
        return super.performNestDistributedTransaction();
    }

    @DasTransactional(logicDbName = DB_NAME)
    public String performDistributedTransaction() {
        return super.performDistributedTransaction();
    }

    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String perform(@Shard String id) {
        return super.perform(id);
    }

    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String perform(@Shard Integer id) {
        return super.perform(id);
    }

    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String perform(@Shard int id) {
        return super.perform(id);
    }

    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String perform(String id, Hints hints) {
        return super.perform(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performFail(String id, Hints hints) {
        return super.performFail(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performWitShard(@Shard String id, Hints hints) {
        return super.performWitShard(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performWitShardNest(@Shard String id, Hints hints) {
        return super.performWitShardNest(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performWitShardNestConflict(@Shard String id, Hints hints) {
        return super.performWitShardNestConflict(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performWitShardNestFail(@Shard String id, Hints hints) {
        return super.performWitShardNestFail(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performCommandWitShardNest(final @Shard String id, Hints hints) throws SQLException {
        return super.performCommandWitShardNest(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performCommandWitShardNestFail(final @Shard String id, Hints hints) throws SQLException {
        return super.performCommandWitShardNestFail(id, hints);
    }
    
    @DasTransactional(logicDbName = DB_NAME_SHARD)
    public String performDetectDistributedTransaction(final @Shard String id, Hints hints) throws SQLException {
        return super.performDetectDistributedTransaction(id, hints);
    }
}
