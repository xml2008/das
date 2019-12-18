package com.ppdai.das.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.ppdai.das.core.configure.DataSourceConfigure;
import com.ppdai.das.core.configure.DataSourceConfigureProvider;
import com.ppdai.das.core.task.TaskFactory;

public class DasConfigure {
    private String appId;
    private Map<String, DatabaseSet> databaseSets = new ConcurrentHashMap<String, DatabaseSet>();
    private DasLogger dalLogger;
    private ConnectionLocator locator;
    private TaskFactory facory;
    private DatabaseSelector selector;

    public DasConfigure(String appId, Map<String, DatabaseSet> databaseSets, DasLogger dalLogger,
            ConnectionLocator locator, TaskFactory facory, DatabaseSelector selector) {
        this.appId = appId;
        this.databaseSets.putAll(databaseSets);
        this.dalLogger = dalLogger;
        this.locator = locator;
        this.facory = facory;
        this.selector = selector;

        ScheduledExecutorService executer = Executors.newScheduledThreadPool(3, r -> {
            Thread thread = new Thread(r, "Das-MGRDatabaseSelector@start: " + new Date());
            thread.setDaemon(true);
            return thread;
        });
        executer.scheduleWithFixedDelay(() ->{check();}, 5, 10, TimeUnit.SECONDS);

    }

    static class MGRInfo {
        String id;
        String host;
        int port;
        String state;
        boolean isMaster;

        public MGRInfo(String id, String host, int port, String state, boolean isMaster) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.state = state;
            this.isMaster = isMaster;
        }
    }

    void check() {
        String sql ="SELECT\n" +
                "MEMBER_ID,\n" +
                "MEMBER_HOST,\n" +
                "MEMBER_PORT,\n" +
                "MEMBER_STATE,\n" +
                "IF(global_status.VARIABLE_NAME IS NOT NULL,\n" +
                "'PRIMARY',\n" +
                "'SECONDARY') AS MEMBER_ROLE\n" +
                "FROM\n" +
                "performance_schema.replication_group_members\n" +
                "LEFT JOIN\n" +
                "performance_schema.global_status ON global_status.VARIABLE_NAME = 'group_replication_primary_member'\n" +
                "AND global_status.VARIABLE_VALUE = replication_group_members.MEMBER_ID;";
        databaseSets.entrySet().forEach(set->{set.getValue().getDatabases().values().forEach(db->{
            try {
                List<MGRInfo> mgrInfos = new ArrayList<>();
                //TODO: query MGR info
                try (Connection connection = locator.getConnection(db.getConnectionString());
                     Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)){
                     while(rs.next()){
                        //Retrieve by column name
                        String id  = rs.getString("MEMBER_ID");
                        String host = rs.getString("MEMBER_HOST");
                        int port = rs.getInt("MEMBER_PORT");
                        String state = rs.getString("MEMBER_STATE");
                        String role = rs.getString("MEMBER_ROLE");

                        mgrInfos.add(new MGRInfo(id, host, port, state, "PRIMARY".equalsIgnoreCase(role)));
                    }

                }
                mgrInfos.forEach(mgrInfo -> {
                    db.setAlias(mgrInfo.id);
                    if(mgrInfo.isMaster && set.getValue().getSlaveDbs().contains(db)) {
                        //Move it to masterDbs, and move others to slaveDbs
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });});
    }

    public static class DatabaseSetChangeEvent {
        private ImmutableMap<String, DatabaseSet> newDatabaseSets;

        public DatabaseSetChangeEvent(Map<String, DatabaseSet> newDatabaseSets) {
            this.newDatabaseSets = ImmutableMap.copyOf(newDatabaseSets);
        }

        public Map<String, DatabaseSet> getNewDatabaseSets() {
            return newDatabaseSets;
        }
    }

    /**
     * The method is invoked when DatabaseSets are updated dynamically
     * @param event
     */
    public synchronized void onDatabaseSetsChanged(DatabaseSetChangeEvent event) {
        Map<String, DatabaseSet> newLogicDatabaseSets = event.getNewDatabaseSets();
        for (String name : newLogicDatabaseSets.keySet()) {
            DatabaseSet newDatabaseSet = newLogicDatabaseSets.get(name);
            DatabaseSet oldDatabaseSet = databaseSets.get(name);
            if (!newDatabaseSet.equals(oldDatabaseSet)) {// Added/updated ones
                databaseSets.put(name, newDatabaseSet);
            }
        }

        //Removed ones
        databaseSets.entrySet().removeIf(entry -> !newLogicDatabaseSets.containsKey(entry.getKey()));
    }

    public static class DataSourceConfigureEvent {
        private ImmutableMap<String, DataSourceConfigure> newDataSourceConfigures;

        public DataSourceConfigureEvent(Map<String, DataSourceConfigure> newDataSourceConfigures) {
            this.newDataSourceConfigures = ImmutableMap.copyOf(newDataSourceConfigures);
        }

        public Map<String, DataSourceConfigure> getNewDataSourceConfigures() {
            return newDataSourceConfigures;
        }
    }

    /**
     * The method is invoked when DatabaseSets are updated dynamically
     * @param event
     */
    public synchronized void onDataSourceConfiguresChanged(DataSourceConfigureEvent event) {
        DataSourceConfigureProvider dataSourceConfigureProvider = locator.getProvider();
        dataSourceConfigureProvider.onConfigChanged(event);
    }

    public String getAppId() {
        return appId;
    }

    public DatabaseSet getDatabaseSet(String logicDbName) {
        if (!databaseSets.containsKey(logicDbName))
            throw new IllegalArgumentException("Can not find definition for Database Set " + logicDbName
                    + ". Please check spelling or define it in Dal.config");

        return databaseSets.get(logicDbName);
    }

    public void warmUpConnections() {
        for (DatabaseSet dbSet : databaseSets.values()) {
            Map<String, DataBase> dbs = dbSet.getDatabases();
            for (DataBase db : dbs.values()) {
                Connection conn = null;
                try {
                    conn = locator.getConnection(db.getConnectionString());
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null)
                        try {
                            conn.close();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    public Set<String> getDatabaseSetNames() {
        return databaseSets.keySet();
    }

    public Set<String> getDataSourceNames() {
        Set<String> alldbs = new HashSet<String>();
        for (DatabaseSet set : this.databaseSets.values()) {
            for (DataBase db : set.getDatabases().values()) {
                alldbs.add(db.getConnectionString());
            }
        }
        return alldbs;
    }

    public DasLogger getDasLogger() {
        return dalLogger;
    }

    public ConnectionLocator getConnectionLocator() {
        return locator;
    }

    public TaskFactory getTaskFacory() {
        return facory;
    }

    public DatabaseSelector getDatabaseSelector() {
        return selector;
    }
}