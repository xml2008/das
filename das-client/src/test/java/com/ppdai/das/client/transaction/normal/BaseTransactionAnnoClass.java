package com.ppdai.das.client.transaction.normal;


import com.ppdai.das.client.DasClientFactory;
import com.ppdai.das.client.Hints;
import com.ppdai.das.client.SqlBuilder;
import com.ppdai.das.client.Transaction;
import com.ppdai.das.core.client.DalTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.SQLException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BaseTransactionAnnoClass {
    private String noShardDb;
    private String shardDb;
    private String query;

    public String getNoShardDb() {
        return noShardDb;
    }

    public String getShardDb() {
        return shardDb;
    }

    public static final String DONE = "done";

    public BaseTransactionAnnoClass(String noShardDb, String shardDb, String query) {
        this.noShardDb = noShardDb;
        this.shardDb = shardDb;
        this.query = query;
    }
    
    @Autowired
    private JustAnotherClass jac;
    
    public JustAnotherClass getJac() {
        return jac;
    }
    
    public String performNormal() {
        assertTrue(!DalTransactionManager.isInTransaction());
        return DONE;
    }

    public String perform() {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(noShardDb, DalTransactionManager.getLogicDbName());
        testQuery(noShardDb);
        return DONE;
    }

    public String performFail() {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(noShardDb, DalTransactionManager.getLogicDbName());
        testQuery(noShardDb);
        throw new RuntimeException();
    }

    public String performNest() {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(noShardDb, DalTransactionManager.getLogicDbName());
        perform();
        return DONE;
    }

    public String performNest2() {
        assertTrue(!DalTransactionManager.isInTransaction());
        perform();
        return DONE;
    }

   public String performNest3() throws InstantiationException, IllegalAccessException {
        assertTrue(!DalTransactionManager.isInTransaction());
        TransactionAnnoClassSqlServer target = DalTransactionManager.create(TransactionAnnoClassSqlServer.class);
        target.perform();
        return DONE;
    }

    public String performNestDistributedTransaction() {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(noShardDb, DalTransactionManager.getLogicDbName());
        perform(1);
        fail();
        return DONE;
    }

    public String performDistributedTransaction() {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(noShardDb, DalTransactionManager.getLogicDbName());
        testQueryFail(shardDb);
        return DONE;
    }

    public String perform(String id) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(shardDb, DalTransactionManager.getLogicDbName());
        assertEquals(id, DalTransactionManager.getCurrentShardId());
        return DONE;
    }

    public String perform(Integer id) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(shardDb, DalTransactionManager.getLogicDbName());
        assertEquals(id.toString(), DalTransactionManager.getCurrentShardId());
        return DONE;
    }

    public String perform(int id) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(String.valueOf(id), DalTransactionManager.getCurrentShardId());
        testQuery(shardDb, Hints.hints().inShard(id));
        return DONE;
    }

    public String perform(String id,  Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(hints.getShard(), DalTransactionManager.getCurrentShardId());
        testQuery(shardDb, hints);
        return DONE;
    }
    
    public String performFail(String id, Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(hints.getShard(), DalTransactionManager.getCurrentShardId());
        testQuery(shardDb);
        throw new RuntimeException();
    }
    
    public String performWitShard(String id, Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        if(id != null)
            assertEquals(id, DalTransactionManager.getCurrentShardId());
        else
            assertEquals(hints.getShard(), DalTransactionManager.getCurrentShardId());
        testQuery(shardDb, hints);
        return DONE;
    }
    
    public String performWitShardNest(String id, Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        if(id != null)
            assertEquals(id, DalTransactionManager.getCurrentShardId());
        else
            assertEquals(hints.getShard(), DalTransactionManager.getCurrentShardId());
        performWitShard(id, hints);
        return DONE;
    }
    
    public String performWitShardNestConflict(String id, Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        assertEquals(id, DalTransactionManager.getCurrentShardId());
        performWitShard(id+id, hints);
        fail();
        return DONE;
    }
    
    public String performWitShardNestFail(String id, Hints hints) {
        assertTrue(DalTransactionManager.isInTransaction());
        if(id != null)
            assertEquals(id, DalTransactionManager.getCurrentShardId());
        else
            assertEquals(hints.getShard(), DalTransactionManager.getCurrentShardId());
        performFail(id, hints.inShard(id));
        return DONE;
    }

    public String performCommandWitShardNest(final String id, Hints hints) throws SQLException {
        DasClientFactory.getClient(shardDb).execute( new Transaction() {
            
            @Override
            public void execute() throws SQLException {
                perform(id, new Hints().inShard(id));
                perform(id, new Hints().inShard(id));
            }
        }, new Hints().inShard(id));
        testQuery(shardDb, Hints.hints().inShard(id));

        return DONE;
    }
    
    public String performCommandWitShardNestFail(final String id, Hints hints) throws SQLException {
        DasClientFactory.getClient(shardDb).execute(new Transaction() {
            
            @Override
            public void execute() throws SQLException {
                perform(id, new Hints().inShard(id));
                perform(id, new Hints().inShard(id));
                performWitShard(id, new Hints().inShard(id));
                performWitShardNest(id, new Hints().inShard(id));
                performFail(id, new Hints().inShard(id));
                fail();
            }
        }, new Hints().inShard(id));
        
        return DONE;
    }
    
    public String performDetectDistributedTransaction(final String id, Hints hints) throws SQLException {
        DasClientFactory.getClient(shardDb).execute(new Transaction() {
            
            @Override
            public void execute() throws SQLException {
                perform(id, new Hints().inShard(id));
                perform(id, new Hints().inShard(id));
                performWitShard(id, new Hints().inShard(id));
                performWitShardNest(id, new Hints().inShard(id));
                performWitShardNest(id+id, new Hints());
                fail();
            }
        }, new Hints().inShard(id));
        
        return DONE;
    }

    //TODO:
    private void testQuery(String db, Hints... hints) {
        try {
         //   new DalQueryDao(db).query(query, new StatementParameters(), new Hints(), Integer.class);
            SqlBuilder sb = new SqlBuilder().appendTemplate(query).intoMap();//setHints(hints);
            if(hints.length == 1) {
                sb.setHints(hints[0]);
            }
            DasClientFactory.getClient(db).query(sb);
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    //TODO:
    private void testQueryFail(String db) {
        try {
          //  new DalQueryDao(db).query(query, new StatementParameters(), new Hints(), Integer.class);
            DasClientFactory.getClient(db).query(new SqlBuilder().appendTemplate(query).intoMap());
            fail();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
