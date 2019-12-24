package com.ppdai.das.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DefaultMGRConfigReader implements MGRConfigReader {

    // MGR-0 -> [ip, port,id...] -> [Database1, Database-2 ...]
    private ConcurrentHashMap<String, List<DasConfigure.MGRInfo>> mgrConfig = new ConcurrentHashMap<>();
    private List<DatabaseSet> mgrDatabaseSet = new ArrayList<>();
    private ConnectionLocator locator;

    public DefaultMGRConfigReader(List<DatabaseSet> mgrDatabaseSet,ConnectionLocator locator) {
        if(mgrDatabaseSet.isEmpty()) {
            return;
        }
        this.mgrDatabaseSet.addAll(mgrDatabaseSet);
        this.locator = locator;

        updateMGRInfo();
        ScheduledExecutorService executer = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "Das-MGRDatabaseReader@start: " + new Date());
            thread.setDaemon(true);
            return thread;
        });

        executer.scheduleWithFixedDelay(() -> {
            try {
                updateMGRInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
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

        for (DatabaseSet set : mgrDatabaseSet) {
            String setName = set.getName();
            for (Map.Entry<String, DataBase> db : set.getDatabases().entrySet()) {
                List<DasConfigure.MGRInfo> mgrInfos = new ArrayList<>();
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

                        DasConfigure.MGRInfo mgrInfo = new DasConfigure.MGRInfo(setName, id, host, port, state, "PRIMARY".equalsIgnoreCase(role));
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

    @Override
    public Map<String, List<DasConfigure.MGRInfo>> readMGRConfig() {
        return mgrConfig;
    }


    @Override
    public long mgrValidate(String connectionString) {
        String sql = "select COUNT_TRANSACTIONS_IN_QUEUE from performance_schema.replication_group_member_stats";

        try (Connection connection = locator.getConnection(connectionString);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                //Retrieve by column name
                long count = rs.getLong("COUNT_TRANSACTIONS_IN_QUEUE");
                return count;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

}
