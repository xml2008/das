package com.ppdai.das.core;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ppdai.das.core.enums.DatabaseCategory.MySql;


public class DefaultMGRConfigReader {

    private static final String MGR_INFO =
            "SELECT  MEMBER_ID, MEMBER_HOST, MEMBER_PORT, MEMBER_STATE, " +
            "IF(global_status.VARIABLE_NAME IS NOT NULL, " +
            "'PRIMARY', " +
            "'SECONDARY') AS MEMBER_ROLE " +
            "FROM performance_schema.replication_group_members " +
            "LEFT JOIN performance_schema.global_status ON global_status.VARIABLE_NAME = 'group_replication_primary_member' " +
            "AND global_status.VARIABLE_VALUE = replication_group_members.MEMBER_ID;";

    private static final String TRANSACTIONS =
            "SELECT COUNT_TRANSACTIONS_IN_QUEUE " +
            "FROM performance_schema.replication_group_member_stats";

    private static final String TRANSACTIONS_BEHIND =
            "SELECT transactions_behind " +
            "FROM sys.gr_member_routing_candidate_status";

    private static final String CHECK_SCHEMA =
            "SELECT COUNT(SCHEMA_NAME) as CNT " +
            "FROM INFORMATION_SCHEMA.SCHEMATA " +
            "WHERE SCHEMA_NAME = 'performance_schema'";

    private Map<String, DatabaseSet> mgrDatabaseSet;
    private Set<String> mrgSet = new HashSet<>();
    private ConnectionLocator locator;

    public DefaultMGRConfigReader(Map<String, DatabaseSet> mgrDatabaseSet,ConnectionLocator locator) {
        this.mgrDatabaseSet = mgrDatabaseSet;
        this.locator = locator;
    }

    public void start() {
        try {
            filterMGR();
            updateMGRInfo();
            if(mgrDatabaseSet.keySet().stream().anyMatch(set -> mrgSet.contains(set))) {
                ScheduledExecutorService executer = Executors.newScheduledThreadPool(3, r -> {
                    Thread thread = new Thread(r, "Das-DefaultMGRConfigReader@start: " + new Date());
                    thread.setDaemon(true);
                    return thread;
                });
                executer.scheduleWithFixedDelay(() -> {
                    try {
                        updateMGRInfo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 3, 3, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterMGR() {
        for(Map.Entry<String, DatabaseSet> set : mgrDatabaseSet.entrySet()) {
            boolean isMGR = set.getValue().getDatabases().values().stream().allMatch(db -> mgrInfo(db.getConnectionString()) != null);
            if(isMGR) {
                mrgSet.add(set.getKey());
            }
        }
    }

    private DataBase.MGRInfo mgrInfo(String connectionString) {
        try (Connection connection = locator.getConnection(connectionString);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(MGR_INFO)) {
             String connectHost = url2Host(connection.getMetaData().getURL());

            while (rs.next()) {
                String host = rs.getString("MEMBER_HOST");
                if (host.equals(connectHost)) {
                    String id = rs.getString("MEMBER_ID");
                    int port = rs.getInt("MEMBER_PORT");
                    String state = rs.getString("MEMBER_STATE");
                    String role = rs.getString("MEMBER_ROLE");
                    return new DataBase.MGRInfo(id, host, state, role);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMGRInfo() throws Exception {
        for (String setName : mgrDatabaseSet.keySet()) {
            if (!mrgSet.contains(setName)) {
                continue;
            }

            DatabaseSet set = mgrDatabaseSet.get(setName);

            Map<String, DataBase> dbSnapshot = new HashMap<>(set.getDatabases());
            AtomicBoolean isChanged = new AtomicBoolean(false);
            for (DataBase db : dbSnapshot.values()) {
                DataBase.MGRInfo info = mgrInfo(db.getConnectionString());
                if (info != null) {
                    if (db.isMaster()) {
                        if (!"PRIMARY".equals(info.role) || !"ONLINE".equals(info.state)) {
                            db.setSlave();
                            isChanged.set(true);
                        }
                    }
                    if (!db.isMaster()) {
                        if ("PRIMARY".equals(info.role) && "ONLINE".equals(info.state)) {
                            db.setMaster();
                            isChanged.set(true);
                        }
                    }
                    db.setMgrId(info.id)
                            .setHost(info.host)
                            .setMgrState(info.state)
                            .setMgrRole(info.role);
                }
            }
            if (isChanged.get()) {
                DatabaseSet newDbSet = set.copy(dbSnapshot);
                mgrDatabaseSet.replace(setName, newDbSet);
            }
        }
    }

    private String url2Host(String url) {
        if (Strings.isNullOrEmpty(url)) {
            return "";
        }
        List<String> split = Splitter.on("jdbc:mysql://").splitToList(url);
        if (split.size() > 1) {//For MySQL only
            return Splitter.on(":").splitToList(split.get(1)).get(0);
        }
        return "";
    }

    public void mgrValidate(DatabaseSet dbSet, SelectionContext context) {
        if(dbSet.getDatabaseCategory() != MySql) {
            return;
        }
        String shard = context.getShard();
        Iterable<DataBase> it = null;
        if(shard == null) {
            it = dbSet.getSlaveDbs().stream().filter(d -> d.getMgrId() != null).collect(Collectors.toList());
        } else {
            it = dbSet.getSlaveDbs(shard).stream().filter(d -> d.getMgrId() != null).collect(Collectors.toList());
        }
        for(DataBase dataBase : it){
            try (Connection connection = locator.getConnection(dataBase.getConnectionString());
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(TRANSACTIONS)) {
                while (rs.next()) {
                    long count = rs.getLong("COUNT_TRANSACTIONS_IN_QUEUE");
                    if(count == -1 || count > 0) {
                        context.setMasterOnly();
                        return;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
