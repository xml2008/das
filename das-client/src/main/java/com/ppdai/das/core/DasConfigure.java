package com.ppdai.das.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
    // MGR-0 -> [ip, port,id...]
    private ConcurrentHashMap<String, List<MGRInfo>> mgrConfig = new ConcurrentHashMap<>();

    public DasConfigure(String appId, Map<String, DatabaseSet> databaseSets, DasLogger dalLogger,
            ConnectionLocator locator, TaskFactory facory, DatabaseSelector selector) {
        this.appId = appId;
        this.databaseSets.putAll(databaseSets);
        this.dalLogger = dalLogger;
        this.locator = locator;
        this.facory = facory;
        this.selector = selector;

        updateMGRInfo();
        registerMGR();

        ScheduledExecutorService executer = Executors.newScheduledThreadPool(3, r -> {
            Thread thread = new Thread(r, "Das-MGRDatabaseSelector@start: " + new Date());
            thread.setDaemon(true);
            return thread;
        });
        executer.scheduleWithFixedDelay(() -> {
            try {
                checkMaster();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);

    }

    private List<DatabaseSet> mgrDatabaseSet() {
       return databaseSets.values().stream().filter(e -> e.getName().startsWith("MGR-")).collect(Collectors.toList());
    }

    private List<DatabaseSet> nonMGRMySQLDatabaseSet() {
        return databaseSets.values().stream().filter(e -> !e.getName().startsWith("MGR-") && e.getDatabaseCategory() == MySql).collect(Collectors.toList());
    }

    static boolean TEST = true;
    private void checkMaster() throws Exception {
        for (DatabaseSet set : nonMGRMySQLDatabaseSet()) {
            AtomicBoolean isMasterChange = new AtomicBoolean(false);
            for (Map.Entry<String, DataBase> dbe : set.getDatabases().entrySet()) {
                DataBase db = dbe.getValue();
                if (db.isMGR()) {
                    List<MGRInfo> infos = mgrConfig.get(db.getMrgName());
                    Optional<MGRInfo> found = infos.stream().filter(info -> info.id.equals(db.getMrgId())).findFirst();
                    if(found.isPresent()) {
                        MGRInfo info = found.get();
                      /*  if(TEST) {
                            if(db.isMaster()) {
                                db.setSlave();
                                isMasterChange.set(true);
                            } else  {
                                db.setMaster();
                                isMasterChange.set(true);
                            }
                        }*/
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

    private MGRInfo findMgrIdByHost(String host) {
        for(Map.Entry<String, List<MGRInfo>> en: mgrConfig.entrySet()) {
            for (MGRInfo info: en.getValue()) {
                if(info.host.equals(host)) {
                    return info;
                }
            }
        }
        return null;
    }

    private void registerMGR() {
        for (DatabaseSet set : nonMGRMySQLDatabaseSet()) {
            for (Map.Entry<String, DataBase> db : set.getDatabases().entrySet()) {
                String hostURL = locator.getProvider().getDataSourceConfigure(db.getKey()).getConnectionUrl();
                if (Strings.isNullOrEmpty(hostURL)) {//TODO:
                    continue;
                }
                List<String> split = Splitter.on("jdbc:mysql://").splitToList(hostURL);
                if (split.size() > 1) {//For MySQL only
                    List<String> host = Splitter.on(":").splitToList(split.get(1));
                    MGRInfo mgr = findMgrIdByHost(host.get(0));
                    if (mgr != null) {
                        db.getValue().setMrgId(mgr.id);
                        db.getValue().setMrgNamed(mgr.name);
                        db.getValue().setHost(host.get(0));
                    }
                }
            }
        }
    }

    static class MGRInfo {
        String name;
        String id;
        String host;
        int port;
        String state;
        boolean isMaster;

        public MGRInfo(String name, String id, String host, int port, String state, boolean isMaster) {
            this.name = name;
            this.id = id;
            this.host = host;
            this.port = port;
            this.state = state;
            this.isMaster = isMaster;
        }
    }

    void updateMGRInfo() {
        String sql = "SELECT\n" +
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

        for (DatabaseSet set : mgrDatabaseSet()) {
            String setName = set.getName();
            for (Map.Entry<String, DataBase> db : set.getDatabases().entrySet()) {
                List<MGRInfo> mgrInfos = new ArrayList<>();
                try (Connection connection = locator.getConnection(db.getValue().getConnectionString());
                     Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        //Retrieve by column name
                        String id = rs.getString("MEMBER_ID");
                        String host = rs.getString("MEMBER_HOST");
                        int port = rs.getInt("MEMBER_PORT");
                        String state = rs.getString("MEMBER_STATE");
                        String role = rs.getString("MEMBER_ROLE");

                        MGRInfo mgrInfo = new MGRInfo(setName, id, host, port, state, "PRIMARY".equalsIgnoreCase(role));
                        mgrInfos.add(mgrInfo);
                    }
                    mgrConfig.put(setName, mgrInfos);
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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