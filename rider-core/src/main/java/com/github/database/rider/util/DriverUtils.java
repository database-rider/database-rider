package com.github.database.rider.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by pestano on 07/09/16.
 */
public class DriverUtils {


    public static boolean isHsql(String driverName) {
        return driverName != null && driverName.contains("hsql");
    }

    public static boolean isH2(String driverName) {
        return driverName != null && driverName.contains("h2");
    }

    public static boolean isMysql(String driverName) {
        return driverName != null && driverName.contains("mysql");
    }

    public static boolean isPostgre(String driverName) {
        return driverName != null && driverName.contains("postgre");
    }

    public static boolean isOracle(String driverName) {
        return driverName != null && driverName.contains("oracle");
    }

    public static String getDriverName(Connection connection) {
        try {
            return connection.getMetaData().getDriverName().toLowerCase();
        } catch (SQLException e) {
            throw new RuntimeException("Could not get driver information from provided connection.",e);
        }
    }

}
