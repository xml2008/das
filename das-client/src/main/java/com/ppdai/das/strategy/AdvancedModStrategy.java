package com.ppdai.das.strategy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author hejiehui
 *
 */
public class AdvancedModStrategy extends AbstractConditionStrategy {
    /**
     * Key used to declared mod for locating DB shards.
     */
    public static final String MOD = "mod";

    /**
     * Key used to declared mod for locating table shards.
     */
    public static final String TABLE_MOD = "tableMod";

    /**
     * Key used to declared table shard suffixLength
     */
    public static final String TABLE_SUFFIX_LENGTH = "tableSuffixLength";

    /**
     * Table shard suffix format, default 1.
     */
    private String tableSuffixLengthFormat = "%01d";

    /**
     * Key used to declared DB shard suffixLength
     */
    public static final String SUFFIX_LENGTH = "suffixLength";

    /**
     * DB shard suffix format, default 1.
     */
    private String suffixLengthFormat = "%01d";
    
    private ModShardLocator<ConditionContext> dbLoactor;
    private ModShardLocator<TableConditionContext> tableLoactor;
    
    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);
        
        if(isShardByDb()) {
            if(!settings.containsKey(MOD))
                throw new IllegalArgumentException("Property " + MOD + " is required for shard by database");

            if(settings.containsKey(SUFFIX_LENGTH))
                suffixLengthFormat = "%0" + Integer.parseInt(settings.get(SUFFIX_LENGTH)) + "d";

            dbLoactor = new ModShardLocator<>(Integer.parseInt(settings.get(MOD)));
        }
        
        if(isShardByTable()) {
            if(!settings.containsKey(TABLE_MOD))
                throw new IllegalArgumentException("Property " + TABLE_MOD + " is required for shard by table");

            if(settings.containsKey(TABLE_SUFFIX_LENGTH))
                tableSuffixLengthFormat = "%0" + Integer.parseInt(settings.get(TABLE_SUFFIX_LENGTH)) + "d";

            Integer mod = Integer.parseInt(settings.get(TABLE_MOD));
            tableLoactor = new ModShardLocator<>(mod);
            
            Set<String> allShards = new HashSet<>();
            for(int i = 0; i < mod; i++)
                allShards.add(String.valueOf(i));
            
            setAllTableShards(allShards);
        }
    }

    @Override
    public Set<String> locateDbShardsByValue(ShardingContext ctx, Object shardValue) {
        Set<String> original = dbLoactor.locateByValue(shardValue);
        return applySuffix(original, suffixLengthFormat);
    }

    @Override
    public Set<String> locateDbShards(ConditionContext ctx) {
        Set<String> original = dbLoactor.locateShards(ctx);
        return applySuffix(original, suffixLengthFormat);
    }

    @Override
    public Set<String> locateTableShardsByValue(TableShardingContext ctx, Object tableShardValue) {
        Set<String> original = tableLoactor.locateByValue(tableShardValue);
        return applySuffix(original, tableSuffixLengthFormat);
    }

    @Override
    public Set<String> locateTableShards(TableConditionContext ctx) {
        Set<String> original = tableLoactor.locateShards(ctx);
        return applySuffix(original, tableSuffixLengthFormat);
    }

    private Set<String> applySuffix(Set<String> original, String format) {
        return original.stream()
                .map(s -> String.format(format, Integer.parseInt(s)))
                .collect(Collectors.toSet());
    }
}