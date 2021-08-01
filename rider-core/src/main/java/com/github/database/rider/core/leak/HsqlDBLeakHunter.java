package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 *
 * Leak hunter for HSQLDB
 */
class HsqlDBLeakHunter extends AbstractLeakHunter {

    private final String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS";

    public HsqlDBLeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        super(connection, methodName, cacheConnection);
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }


}
