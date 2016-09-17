package com.github.database.rider.core.leak;

import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.util.DriverUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 07/09/16.
 */
public class LeakHunterFactory {

    private static final Logger LOG = Logger.getLogger(LeakHunterFactory.class.getName());

    public static LeakHunter from(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new RuntimeException("Cannot create Leak Hunter from a null or closed connection");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        String driverName = DriverUtils.getDriverName(connection);

        if (DriverUtils.isHsql(driverName) || DriverUtils.isH2(driverName)) {
            return new InMemoryLeakHunter(connection);
        } else if (DriverUtils.isPostgre(driverName)) {
            return new PostgreLeakHunter(connection);
        } else if (DriverUtils.isMysql(driverName)) {
            return new MySqlLeakHunter(connection);
        } else if (DriverUtils.isOracle(driverName)) {
            return new OracleLeakHunter(connection);
        }
        return null;
    }
}
