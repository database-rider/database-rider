package com.github.database.rider.core.leak;

import java.sql.Connection;

/**
 * Created by pestano on 07/09/16.
 * <p>
 * Leak hunter for H2
 */
class H2LeakHunter extends AbstractLeakHunter {

    private final String sql = "select count(*) from information_schema.sessions;";

    public H2LeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        super(connection, methodName, cacheConnection);
    }

    @Override
    protected String leakCountSql() {
        return sql;
    }


}
