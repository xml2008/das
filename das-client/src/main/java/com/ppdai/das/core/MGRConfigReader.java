package com.ppdai.das.core;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ppdai.das.core.enums.DatabaseCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class MGRConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(MGRConfigReader.class);

    private Map<String, DatabaseSet> mgrDatabaseSet;
    private Set<String> mrgSet = new HashSet<>();
    private ConnectionLocator locator;

    private static final String PRIMARY = "PRIMARY";
    private static final String SECONDARY = "SECONDARY";
    private static final String ONLINE = "ONLINE";
    private static final String ERROR = "ERROR";
    private static final String MGR_INFO =
            "SELECT  MEMBER_ID, MEMBER_HOST, MEMBER_PORT, MEMBER_STATE, " +
            "IF(global_status.VARIABLE_NAME IS NOT NULL, " +
            "'" + PRIMARY+"', " +
            "'" + SECONDARY +"') AS MEMBER_ROLE " +
            "FROM performance_schema.replication_group_members " +
            "LEFT JOIN performance_schema.global_status ON global_status.VARIABLE_NAME = 'group_replication_primary_member' " +
            "AND global_status.VARIABLE_VALUE = replication_group_members.MEMBER_ID;";

    private static ScheduledExecutorService executer;

    public static void init(DasConfigureContext configContext) {
        executer = Executors.newScheduledThreadPool(configContext.getAppIds().size(), r -> {
            Thread thread = new Thread(r, "MGRConfigReader@start: " + new Date());
            thread.setDaemon(true);
            return thread;
        });
        for(String appId: configContext.getAppIds()) {
            DasConfigure dasConfigure = configContext.getConfigure(appId);
            new MGRConfigReader(dasConfigure.getDatabaseSets(), dasConfigure.getConnectionLocator()).start();
        }
    }

    MGRConfigReader(Map<String, DatabaseSet> mgrDatabaseSet, ConnectionLocator locator) {
        this.mgrDatabaseSet = mgrDatabaseSet;
        this.locator = locator;
    }

    void start() {
        try {
            filterMGR();
            updateMGRInfo();
            if(!mrgSet.isEmpty()) {
                executer.scheduleWithFixedDelay(() -> {
                    try {
                        updateMGRInfo();
                    } catch (Exception e) {
                        logger.error("Exception occurs when updateMGRInfo", e);
                    }
                }, 3, 3, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Exception occurs when start MGRConfigReader.", e);
        }
    }

    private void filterMGR() {
        for(Map.Entry<String, DatabaseSet> set : mgrDatabaseSet.entrySet()) {
            if(set.getValue().getDatabaseCategory() != DatabaseCategory.MySql) {
                continue;
            }
            boolean isMGR = set.getValue().getDatabases().values().stream().allMatch(db -> !ERROR.equals(mgrInfo(db.getConnectionString()).state));
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
            logger.error("Exception occurs when query MGR info.", e);
        }
        return new DataBase.MGRInfo(ERROR, connectionString, ERROR, ERROR);
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

    void updateMGRInfo() throws Exception {
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
                        if (!PRIMARY.equals(info.role) || !ONLINE.equals(info.state)) {
                            db.setSlave();
                            isChanged.set(true);
                        }
                    }
                    if (!db.isMaster()) {
                        if (PRIMARY.equals(info.role) && ONLINE.equals(info.state)) {
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

}
