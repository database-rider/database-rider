package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 */
class PostgreLeakHunter extends AbstractLeakHunter {

    private final String sql = "SELECT COUNT(*) FROM pg_stat_activity WHERE state ILIKE '%idle%'";

    public PostgreLeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        super(connection, methodName, cacheConnection);
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }

}
