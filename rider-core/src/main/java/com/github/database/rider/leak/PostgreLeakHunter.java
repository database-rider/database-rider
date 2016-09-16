package com.github.database.rider.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 */
class PostgreLeakHunter extends AbstractLeakHunter {

    private final String sql = "SELECT COUNT(*) FROM pg_stat_activity WHERE state ILIKE '%idle%'";

    Connection connection;

    public PostgreLeakHunter(Connection connection){
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
