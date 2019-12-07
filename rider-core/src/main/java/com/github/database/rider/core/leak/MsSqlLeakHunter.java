package com.github.database.rider.core.leak;

import java.sql.Connection;

public class MsSqlLeakHunter extends AbstractLeakHunter {
    private static final String ACTIVE_SESSIONS_COUNT_SQL = "select count(*) from sys.sysprocesses where dbid > 0";

    private final Connection connection;

    public MsSqlLeakHunter(Connection connection, String methodName) {
        super(methodName);
        this.connection = connection;
    }

    @Override
    protected String leakCountSql() {
        return ACTIVE_SESSIONS_COUNT_SQL;
    }

    @Override
    protected Connection getConnection() {
        return connection;
    }
}
