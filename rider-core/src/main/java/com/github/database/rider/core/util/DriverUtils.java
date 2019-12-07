package com.github.database.rider.core.util;

import com.github.database.rider.core.spi.DriverNameService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ServiceLoader;

/**
 * Created by pestano on 07/09/16.
 */
public class DriverUtils {

    public static boolean isMsSql(String driverName) {
        return hasKeywordInDriverName(driverName, "sql server");
    }

    public static boolean isHsql(String driverName) {
        return hasKeywordInDriverName(driverName, "hsql");
    }

    public static boolean isH2(String driverName) {
        return hasKeywordInDriverName(driverName, "h2");
    }

    public static boolean isMysql(String driverName) {
        return hasKeywordInDriverName(driverName, "mysql");
    }

    public static boolean isPostgre(String driverName) {
        return hasKeywordInDriverName(driverName, "postgre");
    }

    public static boolean isOracle(String driverName) {
        return hasKeywordInDriverName(driverName, "oracle");
    }

    public static boolean isDB2(String driverName) {
        return hasKeywordInDriverName(driverName, "db2");
    }

    public static String getDriverName(Connection connection) {
        try {
             ServiceLoader<DriverNameService> driverNameService = ServiceLoader.load(DriverNameService.class);
             if(driverNameService.iterator().hasNext()) {
                 return driverNameService.iterator().next().getDriverName(connection);
             }
            return connection.getMetaData().getDriverName().toLowerCase();
        } catch (SQLException e) {
            throw new RuntimeException("Could not get driver information from provided connection.",e);
        }
    }

    private static boolean hasKeywordInDriverName(String driverName, String keyword) {
        return driverName != null && driverName.contains(keyword);
    }

}
