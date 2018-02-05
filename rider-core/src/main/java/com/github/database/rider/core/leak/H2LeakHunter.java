package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 * <p>
 * Leak hunter for H2
 */
class H2LeakHunter extends AbstractLeakHunter {

    private final String sql = "select count(*) from information_schema.sessions;";

    private Connection connection;

    public H2LeakHunter(Connection connection, String methodName) {
        super(methodName);
        this.connection = connection;
    }


    @Override
    protected String leakCountSql() {
        return sql;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
