package com.ppdai.das.core;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ppdai.das.core.enums.DatabaseCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * It launches backend threads to query MRG (MySQL Replication Group) info at interval,
 * to refresh master/slave databases configuration.
 *
 * Two read/write modes:
 * 1: Disable read/write splitting mode
 *    All read/write requests will be routed the single master, no slaves.
 * 2: Disable read/write splitting mode
 *    Write requests will routed to single master, read ones will be routed to slaves.
 *
 */
public class MGRConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(MGRConfigReader.class);

    private Map<String, DatabaseSet> databaseSet;
    private Map<String, DatabaseSet> mgrDatabaseSetSnapshot = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> connectionString2Host = new ConcurrentHashMap<>();
    private ConnectionLocator locator;
    private static AtomicBoolean readWriteSplitting = new AtomicBoolean(false);

    private static final String PRIMARY = "PRIMARY";
    private static final String SECONDARY = "SECONDARY";
    private static final String ONLINE = "ONLINE";

    private static final String MGR_INFO =
            "SELECT  MEMBER_ID, MEMBER_HOST, MEMBER_PORT, MEMBER_STATE, " +
            "IF(global_status.VARIABLE_NAME IS NOT NULL, " +
            "'" + PRIMARY+"', " +
            "'" + SECONDARY +"') AS MEMBER_ROLE " +
            "FROM performance_schema.replication_group_members " +
            "LEFT JOIN performance_schema.global_status ON global_status.VARIABLE_NAME = 'group_replication_primary_member' " +
            "AND global_status.VARIABLE_VALUE = replication_group_members.MEMBER_ID;";

    private ScheduledExecutorService executer;

    public static void enableMGRReadWriteSplitting() {
        readWriteSplitting.set(true);
    }

    public MGRConfigReader(Map<String, DatabaseSet> mgrDatabaseSet, ConnectionLocator locator) {
        this.databaseSet = mgrDatabaseSet;
        this.locator = locator;
        createExecutor(mgrDatabaseSet.size());
    }

    private void createExecutor(int size) {
        if(executer == null) {
            executer = Executors.newScheduledThreadPool(size, r -> {
                Thread thread = new Thread(r, "MGRConfigReader@start: " + new Date());
                thread.setDaemon(true);
                return thread;
            });
        }
    }

    public void start() {
        try {
            filterMGR();
            updateMGRInfo();
            if(!mgrDatabaseSetSnapshot.isEmpty()) {
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

    static class MGRInfo {
        String id;
        String host;
        String state;
        String role;
        String name;

        MGRInfo(String id, String host, String state, String role) {
            this.id = id;
            this.host = host;
            this.state = state;
            this.role = role;
        }

        String getHost() {
            return host;
        }

        boolean isOnline() {
            return ONLINE.equals(state);
        }

        boolean isMaster() {
            return PRIMARY.equals(role);
        }

        boolean isOnlineMaster() {
            return isOnline() && isMaster();
        }
    }
    
    private void filterMGR() throws Exception {
        for(Map.Entry<String, DatabaseSet> set : databaseSet.entrySet()) {
            if(set.getValue().getDatabaseCategory() != DatabaseCategory.MySql) {
                continue;
            }
            boolean isMGR = set.getValue().getDatabases().values().stream().allMatch(db -> !mgrInfoDB(db.getConnectionString()).isEmpty());
            if(isMGR) {
                mgrDatabaseSetSnapshot.put(set.getKey(), set.getValue().deepCopy());
            }
        }
    }

    private Map<String, MGRInfo> mgrInfoMerged(DatabaseSet set) {
        List<MGRInfo> list = new ArrayList<>();
        for (DataBase db : set.getDatabases().values()) {
            List<MGRInfo> mgrs = mgrInfoDB(db.getConnectionString());
            list.addAll(mgrs);
        }
        Map<String, List<MGRInfo>> group = list.stream().collect(Collectors.groupingBy(MGRInfo::getHost));
        Map<String, MGRInfo> result = new HashMap<>();
        for (Map.Entry<String, List<MGRInfo>> en : group.entrySet()) {
            Optional<Map.Entry<String, String>> op = connectionString2Host.entrySet().stream().filter(e -> e.getValue().equals(en.getKey())).findFirst();
            if (op.isPresent()) {
                result.put(op.get().getKey(), en.getValue().stream().findFirst().get());
            }
        }

        return result;
    }

    private List<MGRInfo> mgrInfoDB(String connectionString) {
        List<MGRInfo> list = new ArrayList<>();
        try (Connection connection = locator.getConnection(connectionString);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(MGR_INFO)) {
            String url = connection.getMetaData().getURL();
            connectionString2Host.putIfAbsent(connectionString, url2Host(url));

            while (rs.next()) {
                String host = rs.getString("MEMBER_HOST");
                String id = rs.getString("MEMBER_ID");
                int port = rs.getInt("MEMBER_PORT");
                String state = rs.getString("MEMBER_STATE");
                String role = rs.getString("MEMBER_ROLE");

                list.add(new MGRInfo(id, host, state, role));
            }
        } catch (Exception e) {
            logger.error("Exception occurs when query MGR info.", e);
        }
        return list;
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

    static abstract class MGRStatusHandler {
        static MGRStatusHandler create(boolean isReadWriteSplitting) {
            return isReadWriteSplitting ? new ReadWriteSplittingHandler() : new NonReadWriteSplittingHandler();
        }

        abstract void handle(DatabaseSet set, MGRInfo info, DataBase db);
    }

    /**
     * Disable read/write splitting mode
     */
    private static class NonReadWriteSplittingHandler extends MGRStatusHandler {
        @Override
        void handle(DatabaseSet set, MGRInfo info, DataBase db) {
            if(info != null && info.isOnlineMaster()) {
                db.setMaster();
            } else {
                set.remove(db.getName());
            }
        }
    }

    /**
     * Enable read/write splitting mode
     */
    private static class ReadWriteSplittingHandler extends MGRStatusHandler {
        @Override
        void handle(DatabaseSet set, MGRInfo info, DataBase db) {
            if (info == null || !info.isOnline()) { //Exceptional node
                set.remove(db.getName());
            } else {
                if(info.isMaster()) {
                    db.setMaster();
                } else {
                    db.setSlave();
                }
            }
        }
    }

    void updateMGRInfo() throws Exception {
        for (Map.Entry<String, DatabaseSet> ent : mgrDatabaseSetSnapshot.entrySet()) {
            String setName = ent.getKey();
            DatabaseSet set = ent.getValue();
            DatabaseSet newSet = set.deepCopy();
            Map<String, MGRInfo> infos = mgrInfoMerged(newSet);

            for (DataBase db : newSet.getDatabases().values()) {
                MGRInfo info = infos.get(db.getConnectionString());
                MGRStatusHandler handler = MGRStatusHandler.create(readWriteSplitting.get());
                handler.handle(newSet, info, db);
            }

            newSet.initShards();
            DatabaseSet current = databaseSet.get(setName);
            //Replace databaseSet atomically if changed
            if(!current.equals(newSet)){
                databaseSet.replace(setName, newSet);
                logger.info("Database changes for MGR: " + setName);
            }
        }
    }

}
