package com.github.database.rider.core.leak;

import com.github.database.rider.core.api.leak.LeakHunter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by pestano on 07/09/16.
 */
public abstract class AbstractLeakHunter implements LeakHunter {

    private final String methodName;
    private Integer openConnectionsBeforeExecution;

    public AbstractLeakHunter(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public int openConnections() {
        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(leakCountSql())) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int measureConnectionsBeforeExecution() {
        return openConnectionsBeforeExecution = openConnections();
    }

    @Override
    public void checkConnectionsAfterExecution() throws LeakHunterException {
        if (openConnectionsBeforeExecution == null) {
            throw new IllegalStateException("unknown number of opened connections before execution");
        }

        int openConnectionsAfterExecution = openConnections();

        if (openConnectionsAfterExecution > openConnectionsBeforeExecution) {
            throw new LeakHunterException(methodName, openConnectionsAfterExecution - openConnectionsBeforeExecution);
        }
    }

    protected abstract String leakCountSql();

    protected abstract Connection getConnection();
}
