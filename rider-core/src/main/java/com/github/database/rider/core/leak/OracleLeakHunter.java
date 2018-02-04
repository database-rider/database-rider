package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 */
class OracleLeakHunter extends AbstractLeakHunter {


    private final String sql = "SELECT COUNT(*) FROM v$session WHERE status = 'INACTIVE'";

    Connection connection;

    public OracleLeakHunter(Connection connection, String methodName) {
        super(methodName);
        this.connection = connection;
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }

    @Override
    protected Connection getConnection() {
        return connection;
    }
}
