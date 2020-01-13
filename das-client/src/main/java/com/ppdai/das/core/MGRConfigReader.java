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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private Map<String, DatabaseSet> mgrDatabaseSet;
    private Set<String> mrgSet = new HashSet<>();
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

    public static void enableMGRReadWriteSplitting() {
        readWriteSplitting.set(true);
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
    
    private void filterMGR() {
        for(Map.Entry<String, DatabaseSet> set : mgrDatabaseSet.entrySet()) {
            if(set.getValue().getDatabaseCategory() != DatabaseCategory.MySql) {
                continue;
            }
            boolean isMGR = set.getValue().getDatabases().values().stream().allMatch(db -> !mgrInfoDB(db.getConnectionString()).isEmpty());
            if(isMGR) {
                mrgSet.add(set.getKey());
            }
        }
    }

    private Map<String, MGRInfo> mgrInfoMerged(DatabaseSet set) {
        List<MGRInfo> list = new ArrayList<>();
        for (DataBase db : set.getDatabases().values()) {
            List<MGRInfo> mgrs = mgrInfoDB(db.getConnectionString());
            list.addAll(mgrs);
        }
        Map<String, List<MGRInfo>> m = list.stream().collect(Collectors.groupingBy(MGRInfo::getHost));
        Map<String, MGRInfo> result = new HashMap<>();
        for (Map.Entry<String, List<MGRInfo>> en : m.entrySet()) {
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

        abstract boolean handle(DatabaseSet set, MGRInfo info, DataBase db);
    }

    /**
     * Disable read/write splitting mode
     */
    private static class NonReadWriteSplittingHandler extends MGRStatusHandler {
        @Override
        boolean handle(DatabaseSet set, MGRInfo info, DataBase db) {
            if(info != null && info.isOnlineMaster()) {
                boolean isRecovered = set.getCandidateDbs().removeIf(dName -> dName.equals(db.getName()));
                if(isRecovered) {//recovery master nodes
                    db.setMaster();
                    return true;
                }
            } else {
                if(!set.getCandidateDbs().contains(db.getName())){ //add candidates
                    set.getCandidateDbs().add(db.getName());
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Enable read/write splitting mode
     */
    private static class ReadWriteSplittingHandler extends MGRStatusHandler {
        @Override
        boolean handle(DatabaseSet set, MGRInfo info, DataBase db) {
            if (info == null || !info.isOnline()) { //Exceptional node
                set.getCandidateDbs().add(db.getName());
                return true;

            } else {
                boolean isChanged = false;
                if (db.isMaster()) {
                    if (!info.isMaster()) {//master -> slave
                        db.setSlave();
                        isChanged = true;
                    }
                } else {
                    if (info.isMaster()) { //slave -> master
                        db.setMaster();
                        isChanged = true;
                    }
                }
                boolean isRecovered = set.getCandidateDbs().removeIf(dName -> dName.equals(db.getName()));
                if(isRecovered) {//recovery nodes
                    isChanged = true;
                }
                return isChanged;
            }
        }
    }

    void updateMGRInfo() throws Exception {
        for (String setName : mgrDatabaseSet.keySet()) {
            if (!mrgSet.contains(setName)) {
                continue;
            }

            DatabaseSet set = mgrDatabaseSet.get(setName);
            Map<String, DataBase> dbSnapshot = new HashMap<>(set.getDatabases());
            boolean isChanged = false;
            Map<String, MGRInfo> infos = mgrInfoMerged(set);

            for (DataBase db : dbSnapshot.values()) {
                MGRInfo info = infos.get(db.getConnectionString());
                MGRStatusHandler handler = MGRStatusHandler.create(readWriteSplitting.get());
                boolean isDBChanged = handler.handle(set, info, db);
                if(isDBChanged) {
                    isChanged = true;
                }
            }
            if (isChanged) {
                set.initShards();
            }
        }
    }

}
