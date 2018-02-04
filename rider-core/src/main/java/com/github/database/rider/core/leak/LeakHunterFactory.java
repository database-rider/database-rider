package com.github.database.rider.core.leak;

import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.connection.RiderDataSource;

import java.sql.SQLException;

/**
 * Created by pestano on 07/09/16.
 */
public class LeakHunterFactory {

    public static LeakHunter from(RiderDataSource riderDataSource, String methodName) throws SQLException {

        switch (riderDataSource.getDBType()) {
            case H2:
                return new H2LeakHunter(riderDataSource.getConnection(), methodName);
            case HSQLDB:
                return new HsqlDBLeakHunter(riderDataSource.getConnection(), methodName);
            case POSTGRESQL:
                return new PostgreLeakHunter(riderDataSource.getConnection(), methodName);
            case MYSQL:
                return new MySqlLeakHunter(riderDataSource.getConnection(), methodName);
            case ORACLE:
                return new OracleLeakHunter(riderDataSource.getConnection(), methodName);
            default:
                throw new IllegalArgumentException("unknown db type");
        }
    }
}
