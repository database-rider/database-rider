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
                return new H2LeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            case HSQLDB:
                return new HsqlDBLeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            case POSTGRESQL:
                return new PostgreLeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            case MYSQL:
                return new MySqlLeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            case ORACLE:
                return new OracleLeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            case MSSQL:
                return new MsSqlLeakHunter(riderDataSource.getDBUnitConnection().getConnection(), methodName);
            default:
                throw new IllegalArgumentException("unknown db type");
        }
    }
}
