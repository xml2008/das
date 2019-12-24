package com.ppdai.das.core;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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
    private MGRConfigReader mgrConfigReader;

    public DasConfigure(String appId, Map<String, DatabaseSet> databaseSets, DasLogger dalLogger,
            ConnectionLocator locator, TaskFactory facory, DatabaseSelector selector) {
        this.appId = appId;
        this.databaseSets.putAll(databaseSets);
        this.dalLogger = dalLogger;
        this.locator = locator;
        this.facory = facory;
        this.selector = selector;
        this.mgrConfigReader = new DefaultMGRConfigReader(mgrDatabaseSet(), locator);

        try {
            Map<String, List<MGRInfo>> mgrConfig = mgrConfigReader.readMGRConfig();
            registerMGR(mgrConfig);
            checkMaster(mgrConfig);

            if(!mgrConfig.isEmpty()) {
                ScheduledExecutorService executer = Executors.newScheduledThreadPool(3, r -> {
                    Thread thread = new Thread(r, "Das-MGRDatabaseConfig@start: " + new Date());
                    thread.setDaemon(true);
                    return thread;
                });
                executer.scheduleWithFixedDelay(() -> {
                    try {
                        Map<String, List<MGRInfo>> mgrConfigCheck = mgrConfigReader.readMGRConfig();
                        checkMaster(mgrConfigCheck);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 5, 5, TimeUnit.SECONDS);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<DatabaseSet> mgrDatabaseSet() {
       return databaseSets.values().stream().filter(e -> e.getName().startsWith("MGR-")).collect(Collectors.toList());
    }

    private List<DatabaseSet> nonMGRMySQLDatabaseSet() {
        return databaseSets.values().stream().filter(e -> !e.getName().startsWith("MGR-") && e.getDatabaseCategory() == MySql).collect(Collectors.toList());
    }

    private void checkMaster(Map<String, List<MGRInfo>> mgrConfig) throws Exception {
        for (DatabaseSet set : nonMGRMySQLDatabaseSet()) {
            AtomicBoolean isMasterChange = new AtomicBoolean(false);
            for (Map.Entry<String, DataBase> dbe : set.getDatabases().entrySet()) {
                DataBase db = dbe.getValue();
                if (db.isMGR()) {
                    List<MGRInfo> infos = mgrConfig.get(db.getMrgName());
                    Optional<MGRInfo> found = infos.stream().filter(info -> info.id.equals(db.getMrgId())).findFirst();
                    if(found.isPresent()) {
                        MGRInfo info = found.get();
                        if(info.isMaster && !db.isMaster()) {
                            db.setMaster();
                            isMasterChange.set(true);
                        } else if(!info.isMaster && db.isMaster()) {
                            db.setSlave();
                            isMasterChange.set(true);
                        }
                    }
                }
            }
            if(isMasterChange.get()) {
                set.initShards();
            }
        }
    }

    private MGRInfo findMgrIdByHost(Map<String, List<MGRInfo>> mgrConfig, String host) {
        for(Map.Entry<String, List<MGRInfo>> en: mgrConfig.entrySet()) {
            for (MGRInfo info: en.getValue()) {
                if(info.host.equals(host)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void registerMGR(Map<String, List<MGRInfo>> mgrConfig) {
        for (DatabaseSet set : nonMGRMySQLDatabaseSet()) {
            set.registerConfig(this);
            for (Map.Entry<String, DataBase> db : set.getDatabases().entrySet()) {
                String hostURL = locator.getProvider().getDataSourceConfigure(db.getKey()).getConnectionUrl();
                if (Strings.isNullOrEmpty(hostURL)) {//TODO:
                    continue;
                }
                List<String> split = Splitter.on("jdbc:mysql://").splitToList(hostURL);
                if (split.size() > 1) {//For MySQL only
                    List<String> host = Splitter.on(":").splitToList(split.get(1));
                    MGRInfo mgr = findMgrIdByHost(mgrConfig, host.get(0));
                    if (mgr != null) {
                        DataBase dataBase = db.getValue();
                        dataBase.setMrgId(mgr.id);
                        dataBase.setMrgNamed(mgr.name);
                        dataBase.setHost(mgr.host);

                        mgr.addDataBase(dataBase);
                    }
                }
            }
        }
    }

    public void mgrValidate(DatabaseSet dbSet, SelectionContext context) {

        long transactionsInQueue = -1;
        String shard = context.getShard();
        if(shard == null) {
            for(DataBase dataBase : dbSet.getDatabases().values()){
                transactionsInQueue = mgrConfigReader.mgrValidate(dataBase.getConnectionString());
                if(transactionsInQueue != -1) {
                    break;
                }
            }
        } else {
            for(DataBase dataBase :  Iterables.concat(dbSet.getMasterDbs(shard), dbSet.getSlaveDbs(shard))){
                transactionsInQueue = mgrConfigReader.mgrValidate(dataBase.getConnectionString());
                if(transactionsInQueue != -1) {
                    break;
                }
            }
        }

       if (transactionsInQueue > 0) {//Use master, because data is not sync to slave yet
            context.setMasterOnly();
        }
    }

    static class MGRInfo {
        String name;
        String id;
        String host;
        int port;
        String state;
        boolean isMaster;
        List<DataBase> dataBases = new ArrayList<>();

        public MGRInfo(String name, String id, String host, int port, String state, boolean isMaster) {
            this.name = name;
            this.id = id;
            this.host = host;
            this.port = port;
            this.state = state;
            this.isMaster = isMaster;
        }

        public void addDataBases(List<DataBase> dbs) {
             dataBases.addAll(dbs);
        }

        public void addDataBase(DataBase db) {
            dataBases.add(db);
        }

        @Override
        public String toString() {
            return "MGRInfo{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", state='" + state + '\'' +
                    ", isMaster=" + isMaster +
                    '}';
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