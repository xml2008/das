package com.ppdai.das.core.helper;

import com.mysql.cj.jdbc.ConnectionImpl;

public class MySqlConnectionHelper {

    public static boolean isValid(ConnectionImpl connection, int timeout) {
        return pingInternal(connection, timeout);
    }

    private static boolean pingInternal(ConnectionImpl connection, int timeout) {
        if (connection == null) {
            return false;
        }

        try {
            connection.pingInternal(false, timeout * 1000);
        } catch (Throwable t) {
            return false;
        }

        return true;
    }

}
