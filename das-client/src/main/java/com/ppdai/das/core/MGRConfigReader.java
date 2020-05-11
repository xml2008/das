package com.ppdai.das.core;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ppdai.das.core.configure.DataSourceConfigureConstants;
import com.ppdai.das.core.datasource.PoolPropertiesHolder;
import com.ppdai.das.core.datasource.tomcat.DalTomcatDataSource;
import com.ppdai.das.core.enums.DatabaseCategory;
import com.ppdai.das.core.helper.PoolPropertiesHelper;
import com.ppdai.das.core.status.StatusManager;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
 * 2: Enable read/write splitting mode
 *    Write requests will routed to single master, read ones will be routed to slaves.
 *
 */
public class MGRConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(MGRConfigReader.class);

    private DasConfigure dasConfigure;
    private Map<String, DatabaseSet> mgrDatabaseSetSnapshot = new ConcurrentHashMap<>();
    private Set<String> exceptionalHosts= Collections.newSetFromMap(new ConcurrentHashMap<>());
    private ConcurrentHashMap<String, DSEntity> connectionString2DS = new ConcurrentHashMap<>();
    private static AtomicBoolean readWriteSplitting = new AtomicBoolean(false);
    private static long HEATBEAT_INTERVAL = 3L;
    private static Properties poolProperties;
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

    private ScheduledExecutorService executor;

    public static void enableMGRReadWriteSplitting() {
        readWriteSplitting.set(true);
    }

    public static void setHeartbeatInterval(long intervalInSecond) {
        Preconditions.checkArgument(intervalInSecond >= 3, "Please set MGR heart beat interval >= 3 seconds");
        HEATBEAT_INTERVAL = intervalInSecond;
    }

    public static void setDataSourceConfiguration(Properties properties){
        poolProperties = properties;
    }

    public MGRConfigReader(DasConfigure dasConfigure) {
        this.dasConfigure = dasConfigure;
    }

    private void createExecutor(int size) {
        if(executor == null && size > 0) {
            executor = Executors.newScheduledThreadPool(size, r -> {
                Thread thread = new Thread(r, "MGRConfigReader@start: " + new Date());
                thread.setDaemon(true);
                return thread;
            });
        }
    }

    private boolean isMGRCandidate(DatabaseSet dbSet) {
        return dbSet.getDatabaseCategory() == DatabaseCategory.MySql && dbSet.isMgrEnabled();
    }

    /**
     * Independent data sources for MGR heart beat.
     */
    private void setUpDS() throws Exception {
        Map<String, DalTomcatDataSource> uniq = Maps.newHashMap();
        final ConnectionLocator locator = dasConfigure.getConnectionLocator();
        for (DatabaseSet dbSet : dasConfigure.getDatabaseSets().values()) {
            if(!isMGRCandidate(dbSet)) {
                continue;
            }
            Map<String, DataBase> dbs = dbSet.getDatabases();
            for (DataBase db : dbs.values()) {
                try (Connection conn = locator.getConnection(db.getConnectionString())){
                    final String url = conn.getMetaData().getURL();
                    final String host = host(url);
                    DalTomcatDataSource ds = uniq.get(host);
                    boolean duplicate = true;
                    if(ds == null){
                        PoolProperties properties = PoolPropertiesHolder.getInstance().getPoolProperties(url, conn.getMetaData().getUserName());
                        PoolProperties poolProperties = mergePoolProperties(properties);
                        poolProperties.setUrl(toMGRCatalog(url));

                        ds = new DalTomcatDataSource(poolProperties);
                        uniq.put(host, ds);
                        duplicate = false;
                    }
                    connectionString2DS.put(db.getConnectionString(), new DSEntity(ds, host, duplicate));
                } catch (Exception e) {
                    logger.error("Exception occurs when setUpDS");
                    throw e;
                }
            }
        }
    }

    private String toMGRCatalog(String url) {
        List<String> list = Lists.newArrayList(Splitter.on("/").splitToList(url));
        list.set(list.size() -1, "performance_schema");
        return Joiner.on("/").join(list);
    }

    private String host(String url) {
        List<String> list = Splitter.on("/").omitEmptyStrings().splitToList(url);
        return Splitter.on(":").splitToList(list.get(1)).get(0);
    }

    /**
     * Merge existing data source and special configurations for MGR
     */
    private PoolProperties mergePoolProperties(PoolProperties p) {
        PoolProperties merged = PoolPropertiesHelper.getInstance().copy(p);
        merged.setMinIdle(0); //don't cache connection
        merged.setMinEvictableIdleTimeMillis(1000);

        if(poolProperties == null) {//re-use data source configuration
            return merged;
        }

        String testWhileIdle = poolProperties.getProperty(DataSourceConfigureConstants.TESTWHILEIDLE);
        if(testWhileIdle != null){
            merged.setTestWhileIdle(Boolean.valueOf(testWhileIdle));
        }

        String testOnBorrow = poolProperties.getProperty(DataSourceConfigureConstants.TESTONBORROW);
        if(testOnBorrow != null){
            merged.setTestOnBorrow(Boolean.valueOf(testOnBorrow));
        }

        String validationInterval = poolProperties.getProperty(DataSourceConfigureConstants.VALIDATIONINTERVAL);
        if(validationInterval != null){
            merged.setValidationInterval(Long.parseLong(validationInterval));
        }

        String maxAge = poolProperties.getProperty(DataSourceConfigureConstants.MAX_AGE);
        if(maxAge != null){
            merged.setMaxAge(Long.parseLong(maxAge));
        }

        String validationQueryTimeout = poolProperties.getProperty(DataSourceConfigureConstants.VALIDATIONQUERYTIMEOUT);
        if(validationQueryTimeout != null){
            merged.setValidationInterval(Long.parseLong(validationQueryTimeout));
        }

        String validationQuery = poolProperties.getProperty(DataSourceConfigureConstants.VALIDATIONQUERY);
        if(validationQuery != null){
            merged.setValidationQuery(validationQuery);
        }

        return merged;
    }

    public void start() throws Exception {
        setUpDS();
        filterMGR();
        updateMGRInfo(true);
        logger.info("MGR mode: " + (!readWriteSplitting.get() ?  "non-" : "") + "Read/Write splitting");
        logger.info("MGR database sets: " + mgrDatabaseSetSnapshot.keySet());
        logger.info("Databases init status after MGR check: " + dasConfigure.getDatabaseSets());

        if (!mgrDatabaseSetSnapshot.isEmpty()) {
            createExecutor(connectionString2DS.size());
            executor.scheduleWithFixedDelay(() -> {
                try {
                    updateMGRInfo(false);
                } catch (Exception e) {
                    logger.error("Exception occurs when updateMGRInfo", e.getMessage());
                }
            }, 3, HEATBEAT_INTERVAL, TimeUnit.SECONDS);
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
        for(Map.Entry<String, DatabaseSet> setEnt : dasConfigure.getDatabaseSets().entrySet()) {
            DatabaseSet set = setEnt.getValue();
            if(!isMGRCandidate(set)){
                continue;
            }
            boolean isMGR = set.getDatabases().values().stream().anyMatch(db -> !mgrInfoDB(db.getConnectionString(), 5).isEmpty());
            if(isMGR) {
                mgrDatabaseSetSnapshot.put(setEnt.getKey(), set.deepCopy(set.getDatabases()));
            } else {
                logger.error(set.getName() + " is NOT MGR node");
            }
        }
    }

    private List<MGRInfo> mgrInfoDB(String connectionString, int timeout) {
        List<MGRInfo> list = new ArrayList<>();
        DSEntity dsEntity = connectionString2DS.get(connectionString);
        String dbHost = dsEntity.getHost();
        if(exceptionalHosts.contains(dbHost)) {
            return new ArrayList<>();
        }

        try (Connection connection = dsEntity.getDs().getConnection();
             Statement stmt = connection.createStatement()){
             stmt.setQueryTimeout(timeout);
             try(ResultSet rs = stmt.executeQuery(MGR_INFO)) {
                 while (rs.next()) {
                     String host = rs.getString("MEMBER_HOST");
                     String id = rs.getString("MEMBER_ID");
                     int port = rs.getInt("MEMBER_PORT");
                     String state = rs.getString("MEMBER_STATE");
                     String role = rs.getString("MEMBER_ROLE");

                     list.add(new MGRInfo(id, host, state, role));
                 }
             }
        } catch (SQLTimeoutException timeoutException){
            logger.error("MGR heartbeat timeout: " + timeoutException.getMessage());
        }catch (Exception e) {
            logger.error("MGR heartbeat exception:" + e.getMessage());
        }
        return list;
    }

    interface MGRStatusHandler {
        DatabaseSet createDatabaseSet(DatabaseSet set, Map<String, MGRInfo> infos) throws Exception;
    }

    /**
     * Disable read/write splitting mode
     */
    private class NonReadWriteSplittingHandler implements MGRStatusHandler {
        @Override
        public DatabaseSet createDatabaseSet(DatabaseSet set, Map<String, MGRInfo> infos) throws Exception {
            Map<String, DataBase> newDBs = new HashMap<>();
            for(Map.Entry<String, DataBase> dbEnt : set.getDatabases().entrySet()) {
                DataBase db = dbEnt.getValue();
                String host = MGRConfigReader.this.connectionString2DS.get(db.getConnectionString()).getHost();
                MGRInfo info = infos.get(host);
                if(info != null && info.isOnlineMaster()) {
                    newDBs.put(dbEnt.getKey(), db.deepCopy(true));
                }
            }
            return set.deepCopy(newDBs);
        }
    }

    /**
     * Enable read/write splitting mode
     */
    private class ReadWriteSplittingHandler implements MGRStatusHandler {
        @Override
        public DatabaseSet createDatabaseSet(DatabaseSet set, Map<String, MGRInfo> infos) throws Exception {
            Map<String, DataBase> newDBs = new HashMap<>();
            for(Map.Entry<String, DataBase> dbEnt : set.getDatabases().entrySet()) {
                DataBase db = dbEnt.getValue();
                String host = MGRConfigReader.this.connectionString2DS.get(db.getConnectionString()).getHost();
                MGRInfo info = infos.get(host);
                if(info != null && info.isOnline()){
                    newDBs.put(dbEnt.getKey(), db.deepCopy(info.isMaster()));
                }
            }
            return set.deepCopy(newDBs);
        }
    }

    void updateMGRInfo(boolean isInit) throws Exception {
        List<MGRInfo> list = new ArrayList<>();
        for(Map.Entry<String, DSEntity> ent : connectionString2DS.entrySet()) {
            if(ent.getValue().isDuplicate()){
                continue;
            }
            List<MGRInfo> info = mgrInfoDB(ent.getKey(), 1);
            list.addAll(info);
        }

        Map<String, List<MGRInfo>> group = list.stream().collect(Collectors.groupingBy(MGRInfo::getHost));
        Map<String, MGRInfo> infos = group.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            return e.getValue().stream().findFirst().get();
                        }));
        updateNodeStatus(infos);

        for (Map.Entry<String, DatabaseSet> ent : mgrDatabaseSetSnapshot.entrySet()) {
            String setName = ent.getKey();
            DatabaseSet set = ent.getValue();

            MGRStatusHandler handler = readWriteSplitting.get() ? new ReadWriteSplittingHandler() : new NonReadWriteSplittingHandler();
            DatabaseSet newSet = handler.createDatabaseSet(set, infos);
            DatabaseSet current = dasConfigure.getDatabaseSets().get(setName);
            //Replace databaseSet atomically if changed
            if(!current.equals(newSet)){
                dasConfigure.onDatabaseSetChanged(new DasConfigure.DatabaseSetChangeEvent(ImmutableMap.of(setName, newSet)));
                if(!isInit) {
                    for(String appId : DasConfigureFactory.getAppIds()) {
                        StatusManager.registerApplication(appId, dasConfigure);
                    }
                }
                logger.info("Database changes for MGR: " + setName);
            }
        }
    }

    private void updateNodeStatus(Map<String, MGRInfo> infos) {
        for(MGRInfo info : infos.values()){
            if (info.isOnline()){
                if(exceptionalHosts.remove(info.getHost())){
                    logger.info("MGR node: " + info.getHost() + " is back to ONLINE.");
                }
            }else {
                if(exceptionalHosts.add(info.getHost())){
                    logger.warn("MGR node: " + info.getHost() + " is NOT ONLINE any more.");
                    if(exceptionalHosts.size() == 3){
                        logger.error("All MGR nodes are down!");
                    }
                }
            }
        }
    }

    static class DSEntity {
        DalTomcatDataSource ds;
        String host;
        boolean duplicate = false;

        DSEntity(DalTomcatDataSource ds, String host, boolean duplicate) {
            this.ds = ds;
            this.host = host;
            this.duplicate = duplicate;
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        DalTomcatDataSource getDs() {
            return ds;
        }

        String getHost() {
            return host;
        }
    }

}
