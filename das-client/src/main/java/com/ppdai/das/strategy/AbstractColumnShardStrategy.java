package com.ppdai.das.strategy;

import com.ppdai.das.client.Hints;
import com.ppdai.das.client.Parameter;
import com.ppdai.das.core.DasConfigure;
import com.ppdai.das.core.HintEnum;
import com.ppdai.das.core.enums.ParameterDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractColumnShardStrategy extends AbstractRWSeparationStrategy {
    /**
     * Key used to declared columns for locating DB shard.
     */
    public static final String COLUMNS = "columns";
    private static final String COLUMNS_CSHARP = "column";

    /**
     * Key used to declared tables that qualified for table shard. That's not every table is sharded
     */
    public static final String SHARDED_TABLES = "shardedTables";
    
    /**
     * Key used to declared columns for locating table shard.
     */
    public static final String TABLE_COLUMNS = "tableColumns";
    private static final String TABLE_COLUMNS_CSHARP = "tableColumn";
    
    public static final String SEPARATOR = "separator";

    private Set<String> columnNames;
    private Set<String> shardedTables = new HashSet<>();
    private Set<String> tableColumnNames;
    private String separator;
    
    /**
     * columns are separated by ','
     * @Override
     */
    public void initialize(Map<String, String> settings) {
        if(settings.containsKey(COLUMNS)) {
            columnNames = parseNames(settings.get(COLUMNS));
        }else {
            if(settings.containsKey(COLUMNS_CSHARP)) {
                columnNames = parseNames(settings.get(COLUMNS_CSHARP));
            }
        }
        
        if(settings.containsKey(SHARDED_TABLES)) {
            shardedTables = parseNames(settings.get(SHARDED_TABLES));
        }
        
        if(settings.containsKey(TABLE_COLUMNS)) {
            tableColumnNames = parseNames(settings.get(TABLE_COLUMNS));
        }else {
            if(settings.containsKey(TABLE_COLUMNS_CSHARP)) {
                tableColumnNames = parseNames(settings.get(TABLE_COLUMNS_CSHARP));
            }
        }
        
        if(settings.containsKey(SEPARATOR)) {
            separator = settings.get(SEPARATOR);
        }
    }
    
    private Set<String> parseNames(String value) {
        String[] names = value.split(",");
        Set<String> nameSet = new HashSet<>();
        for(int i = 0; i < names.length; i++)
            nameSet.add(names[i].toLowerCase().trim());
        return nameSet;
    }

    @Override
    public boolean isShardingByDb() {
        return columnNames != null;
    }
    
    /**
     * Locate DB shard value
     * @param value column or parameter value
     * @return DB shard id
     */
    abstract public String calculateDbShard(Object value);
    
    /**
     * Locate table shard value
     * @param tableName the rawTableName table name template without any sharding separator or shard id suffix 
     * @param value column or parameter value
     * @return table shard id
     */
    abstract public String calculateTableShard(String rawTableName, Object value);

    public String locateDbShard(DasConfigure configure, String logicDbName,
                                Hints hints) {
        if(!isShardingByDb())
            throw new RuntimeException(String.format("Logic Db %s is not configured to be shard by database", logicDbName));
        
        String shard = hints.getShard();
        if(shard != null)
            return shard;
        
        // Shard value take the highest priority
        if(hints.is(HintEnum.shardValue)) {
            return calculateDbShard(hints.get(HintEnum.shardValue));
        }
        
        shard = evaluateDbShard(columnNames, (Map<String, ?>)hints.get(HintEnum.shardColValues));
        if(shard != null)
            return shard;
        
        shard = evaluateDbShard(columnNames, (List<Parameter>)hints.get(HintEnum.parameters));
        if(shard != null)
            return shard;
        
        shard = evaluateDbShard(columnNames, (Map<String, ?>)hints.get(HintEnum.fields));
        if(shard != null)
            return shard;
        
        return null;
    }

    @Override
    public boolean isShardingByTable() {
        return tableColumnNames != null;
    }

    @Override
    public String locateTableShard(DasConfigure configure, String logicDbName, String tableName,
                                   Hints hints) {
        if(!isShardingByTable())
            throw new RuntimeException(String.format("Logic Db %s is not configured to be shard by table", logicDbName));
        
        String shard = hints.getTableShard();
        if(shard != null)
            return shard;
        
        // Shard value take the highest priority
        if(hints.is(HintEnum.tableShardValue)) {
            return calculateTableShard(tableName, hints.get(HintEnum.tableShardValue));
        }
        
        shard = evaluateTableShard(tableName, tableColumnNames, (Map<String, ?>)hints.get(HintEnum.shardColValues));
        if(shard != null)
            return shard;
        
        shard = evaluateTableShard(tableName, tableColumnNames, (List<Parameter>)hints.get(HintEnum.parameters));
        if(shard != null)
            return shard;
        
        shard = evaluateTableShard(tableName, tableColumnNames, (Map<String, ?>)hints.get(HintEnum.fields));
        if(shard != null)
            return shard;
        
        return null;
    }
    
    private String evaluateDbShard(Set<String> columns, List<Parameter> parameters) {
        Object value = findValue(columns, parameters);
        return value == null ? null : calculateDbShard(value);
    }
    
    private String evaluateTableShard(String tableName, Set<String> columns, List<Parameter> parameters) {
        Object value = findValue(columns, parameters);
        return value == null ? null : calculateTableShard(tableName, value);
    }
    
    private Object findValue(Set<String> columns, List<Parameter> parameters) {
        if(parameters == null)
            return null;

        for(String column: columns) {
            Parameter param = get(parameters, column, ParameterDirection.Input);
            if(param == null || param.getValue() == null)
                continue;
            return param.getValue();
        }

        return null;
    }

    private Parameter get(List<Parameter> parameters, String name, ParameterDirection direction) {
        if(name == null)
            return null;

        for(Parameter parameter: parameters) {
            if(parameter.getName() != null && parameter.getName().equalsIgnoreCase(name) && direction == parameter.getDirection())
                return parameter;
        }
        return null;
    }

    private String evaluateDbShard(Set<String> columns, Map<String, ?> shardColValues) {
        Object value = findValue(columns, shardColValues);
        return value == null ? null : calculateDbShard(value);
    }
    
    private String evaluateTableShard(String tableName, Set<String> columns, Map<String, ?> shardColValues) {
        Object value = findValue(columns, shardColValues);
        return value == null ? null : calculateTableShard(tableName, value);
    }
    
    private Object findValue(Set<String> columns, Map<String, ?> colValues) {
        if(colValues == null)
            return null;            
        
        Object value;
        
        for(String column: columns) {
            value = colValues.get(column);
            if(value != null)
                return value;
        }
        
        //To check in case insensitive way
        for(Map.Entry<String, ?> colEntry: colValues.entrySet()) {
            if(columns.contains(colEntry.getKey().toLowerCase())) {
                value = colEntry.getValue();
                if(value != null)
                    return value;
            }
        }

        return null;            
    }
    
    @Override
    public boolean isShardingEnable(String tableName) {
        return shardedTables.contains(tableName.toLowerCase());
    }

    @Override
    public String getTableShardSeparator() {
        return separator;
    }
    
    public boolean isDbShardColumn(String columnName) {
        return columnNames.contains(columnName.toLowerCase());
    }

    public boolean isTableShardColumn(String columnName) {
        return tableColumnNames.contains(columnName.toLowerCase());
    }
}