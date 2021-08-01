package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 */
class OracleLeakHunter extends AbstractLeakHunter {


    private final String sql = "SELECT COUNT(*) FROM v$session WHERE status = 'INACTIVE'";

    public OracleLeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        super(connection, methodName, cacheConnection);
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }

}
