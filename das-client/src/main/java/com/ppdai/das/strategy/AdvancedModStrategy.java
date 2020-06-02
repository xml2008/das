package com.ppdai.das.strategy;

import com.google.common.base.Strings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author hejiehui
 *
 */
public class AdvancedModStrategy extends AbstractConditionStrategy {
    /**
     * Key used to declared mod type.
     */
    public static final String TYPE = "type";

    /**
     * Key used to declared mod for locating DB shards.
     */
    public static final String MOD = "mod";

    /**
     * Key used to declared mod for locating table shards.
     */
    public static final String TABLE_MOD = "tableMod";

    /**
     * Key used to declared table shard table 0 padding
     */
    private static final String TABLE_ZERO_PADDING = "tableZeroPadding";

    /**
     * Key used to declared DB shard 0 padding
     */
    private static final String ZERO_PADDING = "zeroPadding";
    
    private ModShardLocator<ConditionContext> dbLoactor;
    private ModShardLocator<TableConditionContext> tableLoactor;
    
    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);

        String type = settings.get(TYPE);
        if(isShardByDb()) {
            if(!settings.containsKey(MOD)) {
                throw new IllegalArgumentException("Property " + MOD + " is required for shard by database");
            }

            String zeroPaddingFormat = "%01d";
            if(settings.containsKey(ZERO_PADDING)) {
                zeroPaddingFormat = "%0" + Integer.parseInt(settings.get(ZERO_PADDING)) + "d";
            }

            dbLoactor = createLocator(type, Integer.parseInt(settings.get(MOD)), zeroPaddingFormat);
        }
        
        if(isShardByTable()) {
            if(!settings.containsKey(TABLE_MOD)) {
                throw new IllegalArgumentException("Property " + TABLE_MOD + " is required for shard by table");
            }

            String tableZeroPaddingFormat = "%01d";
            if(settings.containsKey(TABLE_ZERO_PADDING)) {
                tableZeroPaddingFormat = "%0" + Integer.parseInt(settings.get(TABLE_ZERO_PADDING)) + "d";
            }

            Integer mod = Integer.parseInt(settings.get(TABLE_MOD));
            tableLoactor = createLocator(type, mod, tableZeroPaddingFormat);
            
            Set<String> allShards = new HashSet<>();
            for(int i = 0; i < mod; i++) {
                allShards.add(String.format(tableZeroPaddingFormat, i));
            }
            
            setAllTableShards(allShards);
        }
    }

    protected <T extends ConditionContext> ModShardLocator<T> createLocator(String type, int mod, String zeroPaddingFormat) {
        if(Strings.isNullOrEmpty(type)){ //for version compatibility
            return new ModShardLocator<>(mod, zeroPaddingFormat);
        }

        if(type.equalsIgnoreCase("crc")) {
            return new CRCModShardLocator<>(mod, zeroPaddingFormat);
        }

        if(type.equalsIgnoreCase("md5")) {
            return new HashModShardLocator<>(mod, zeroPaddingFormat);
        }

        throw new IllegalArgumentException("Property " + TYPE + " is required 'crc' or 'md5'.");
    }

    @Override
    public Set<String> locateDbShardsByValue(ShardingContext ctx, Object shardValue) {
        return dbLoactor.locateByValue(shardValue);
    }

    @Override
    public Set<String> locateDbShards(ConditionContext ctx) {
        return dbLoactor.locateShards(ctx);
    }

    @Override
    public Set<String> locateTableShardsByValue(TableShardingContext ctx, Object tableShardValue) {
        return tableLoactor.locateByValue(tableShardValue);
    }

    @Override
    public Set<String> locateTableShards(TableConditionContext ctx) {
        return tableLoactor.locateShards(ctx);
    }
}