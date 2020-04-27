package com.ppdai.das.strategy;

import com.ppdai.das.client.Hints;
import com.ppdai.das.core.DasConfigure;
import com.ppdai.das.core.DasConfigureFactory;
import com.ppdai.das.core.HintEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRWSeparationStrategy implements ShardingStrategy {


    /**
     * Check if the shard is by DB
     * @return
     */
    abstract boolean isShardingByDb();

    /**
     * Locate target shard that the operation is performed.
     * If this operation requires cross shard execution, using Cross Shard Manager.
     * @param configure
     * @param logicDbName
     * @param hints
     * @return shard Id for DB, null if not located
     */
    abstract String locateDbShard(DasConfigure configure, String logicDbName, Hints hints);

    /**
     * Check if the shard is by table
     * @return
     */
    abstract boolean isShardingByTable();


    /**
     * Get the separator between raw table name and table shard id
     * @return shard Id for table, null if not located
     */
    abstract String getTableShardSeparator();


	/**
	 * This method is a default implementation old interface defined in DalShardingStrategy.
	 * It just route to new method with no table name
	 * 
	 * @param configure
	 * @param logicDbName
	 * @param hints
	 * @return
	 * @deprecated should use locateTableShard with table name parameter
	 */
	public String locateTableShard(DasConfigure configure, String logicDbName, Hints hints) {
	    return locateTableShard(configure, logicDbName, null, hints);
	}
	
	/**
	 * Call the old way of getting table shard id to make sure subclass compiles ok and not 
	 * break existing logic
	 */
	public String locateTableShard(DasConfigure configure, String logicDbName, String tabelName, Hints hints) {
	    return locateTableShard(configure, logicDbName, hints);
	}
	
    @Override
    public boolean isShardByDb() {
        return isShardingByDb();
    }

    @Override
    public boolean isShardByTable() {
        return isShardingByTable();
    }
    
    @Override
    public Set<String> getAllTableShards() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<String> locateDbShards(ShardingContext ctx) {
        return toSet(locateDbShard(DasConfigureFactory.getConfigure(ctx.getAppId()), ctx.getLogicDbName(), ctx.getHints()));
    }

    @Override
    public Set<String> locateTableShards(TableShardingContext ctx) {
        DasConfigure config = DasConfigureFactory.getConfigure(ctx.getAppId());
        if(ctx.getHints().is(HintEnum.tableShard))
            return toSet(ctx.getHints().getTableShard());

        if(ctx.getHints().is(HintEnum.tableShardValue))
            return toSet(locateTableShard(config, ctx.getLogicDbName(), ctx.getLogicTableName(), ctx.getHints()));

        Set<String> shards = new HashSet<>();
        Hints hints = ctx.getHints().clone();
        for(Condition con: ctx.getConditions()) {
            for(String tableName :con.getTables()) {
                if(isShardingEnable(tableName) && con instanceof ColumnCondition) {
                    ColumnCondition colCon = (ColumnCondition)con;
                    if(colCon.getOperator().equals(OperatorEnum.EQUAL)) {
                        Map<String, Object> tmpFields = new HashMap<>();
                        tmpFields.put(colCon.getColumnName(), colCon.getValue());
                        String shardId = locateTableShard(config, ctx.getLogicDbName(), tableName, hints.setFields(tmpFields));
                        if(shardId != null) {
                            shards.add(shardId);
                        }
                        
                    }
                }
            }
        }
        
        return shards;
    }

    @Override
    public String getTableName(String logicTableName, String shard) {
        return getTableShardSeparator() == null ? logicTableName + shard : logicTableName + getTableShardSeparator() + shard ;
    }

    private Set<String> toSet(String shard) {
        Set<String> shards = new HashSet<>();
        
        if(shard != null)
            shards.add(shard);

        return shards;
    }
}
