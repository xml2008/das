package com.ppdai.das.core;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.ppdai.das.core.configure.DataSourceConfigure;
import com.ppdai.das.core.configure.DataSourceConfigureProvider;
import com.ppdai.das.core.task.TaskFactory;

import static com.ppdai.das.core.enums.DatabaseCategory.MySql;

public class DasConfigure {
    private String appId;
    private Map<String, DatabaseSet> databaseSets = new ConcurrentHashMap<String, DatabaseSet>();
    private DasLogger dalLogger;
    private ConnectionLocator locator;
    private TaskFactory facory;
    private DatabaseSelector selector;
    private DefaultMGRConfigReader mgrConfigReader;

    public DasConfigure(String appId, Map<String, DatabaseSet> databaseSets, DasLogger dalLogger,
            ConnectionLocator locator, TaskFactory facory, DatabaseSelector selector) {
        this.appId = appId;
        this.databaseSets.putAll(databaseSets);
        this.dalLogger = dalLogger;
        this.locator = locator;
        this.facory = facory;
        this.selector = selector;
        this.mgrConfigReader = new DefaultMGRConfigReader(mySQLDatabaseSet(), locator);

        try {
            mgrConfigReader.updateMGRInfo();

            if(!mgrConfigReader.getMgrDatabaseSet().isEmpty()) {
                mgrConfigReader.getMgrDatabaseSet().forEach(db -> db.registerConfig(this));
                ScheduledExecutorService executer = Executors.newScheduledThreadPool(3, r -> {
                    Thread thread = new Thread(r, "Das-MGRDatabaseConfig@start: " + new Date());
                    thread.setDaemon(true);
                    return thread;
                });
                executer.scheduleWithFixedDelay(() -> {
                    try {
                        mgrConfigReader.updateMGRInfo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 3, 3, TimeUnit.SECONDS);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<DatabaseSet> mySQLDatabaseSet() {
        return databaseSets.values().stream().filter(e ->  e.getDatabaseCategory() == MySql).collect(Collectors.toList());
    }

    public void mgrValidate(DatabaseSet dbSet, SelectionContext context) {
        long transactionsInQueue = -1;
        String shard = context.getShard();
        Iterable<DataBase> it = null;
        if(shard == null) {
            it = dbSet.getDatabases().values().stream().filter(d -> d.getMgrId() != null).collect(Collectors.toList());
        } else {
            Iterable<DataBase> master = dbSet.getMasterDbs(shard).stream().filter(d -> d.getMgrId() != null).collect(Collectors.toList());
            Iterable<DataBase> slaves = dbSet.getSlaveDbs(shard).stream().filter(d -> d.getMgrId() != null).collect(Collectors.toList());
            it = Iterables.concat(master, slaves);
        }
        for(DataBase dataBase : it){
            transactionsInQueue = mgrConfigReader.mgrValidate(dataBase.getConnectionString());
            if(transactionsInQueue != -1) {
                break;
            }
        }

        if (transactionsInQueue > 0) {//Use master, because data is not sync to slave yet
            context.setMasterOnly();
        }
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