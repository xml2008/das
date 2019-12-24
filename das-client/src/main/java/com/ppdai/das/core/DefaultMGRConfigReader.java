package com.ppdai.das.core;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


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

    private List<DatabaseSet> mgrDatabaseSet = new ArrayList<>();
    private ConnectionLocator locator;

    public DefaultMGRConfigReader(List<DatabaseSet> mgrDatabaseSet,ConnectionLocator locator) {
        this.mgrDatabaseSet.addAll(mgrDatabaseSet);
        this.locator = locator;
    }

    public List<DatabaseSet> getMgrDatabaseSet() {
        return mgrDatabaseSet;
    }

    static class MGRInfo {
        String id;
        String host;
        String state;
        String role;

        public MGRInfo(String id, String host, String state, String role) {
            this.id = id;
            this.host = host;
            this.state = state;
            this.role = role;
        }
    }

    public void updateMGRInfo() throws Exception {
        Set<DatabaseSet> exceptionalSet = new HashSet<>();
        for (DatabaseSet set : mgrDatabaseSet) {
            AtomicBoolean isChanged = new AtomicBoolean(false);
            for (DataBase db : set.getDatabases().values()) {
                try (Connection connection = locator.getConnection(db.getConnectionString());
                     Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(MGR_INFO)) {

                     String connectHost = url2Host(connection.getMetaData().getURL());
                     Map<String, MGRInfo> infos = new HashMap<>();
                     while (rs.next()) {
                        //Retrieve by column name
                        String id = rs.getString("MEMBER_ID");
                        String host = rs.getString("MEMBER_HOST");
                        int port = rs.getInt("MEMBER_PORT");
                        String state = rs.getString("MEMBER_STATE");
                        String role = rs.getString("MEMBER_ROLE");

                        infos.put(host, new MGRInfo(id, host, state, role));
                    }
                    MGRInfo info = infos.get(connectHost);
                    if(info != null) {
                        db.setMgrId(info.id)
                                .setHost(info.host)
                                .setMgrState(info.state)
                                .setMgrRole(info.role);
                        if ("PRIMARY".equals(info.role) && "ONLINE".equals(info.state) && !db.isMaster()) {
                            db.setMaster();
                            isChanged.set(true);
                        }
                        if ("SECONDARY".equals(info.role) && "ONLINE".equals(info.state) && db.isMaster()) {
                            db.setSlave();
                            isChanged.set(true);
                        }
                    }
                } catch (MySQLSyntaxErrorException ex) {
                    exceptionalSet.add(set);
                }
            }
            if(isChanged.get()) {
                set.initShards();
            }
        }
        mgrDatabaseSet.removeAll(exceptionalSet);
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

    public long mgrValidate(String connectionString) {
        try (Connection connection = locator.getConnection(connectionString);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(TRANSACTIONS)) {
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
