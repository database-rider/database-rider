package com.github.database.rider.core.leak;

import com.github.database.rider.core.api.leak.LeakHunter;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by pestano on 07/09/16.
 */
public abstract class AbstractLeakHunter implements LeakHunter {

    protected final Connection connection;
    private final String methodName;
    private final boolean cacheConnection;
    private Integer openConnectionsBeforeExecution;

    public AbstractLeakHunter(Connection connection, String methodName, boolean cacheConnection) {
        this.methodName = methodName;
        this.cacheConnection = cacheConnection;
        this.connection = connection;
    }

    @Override
    public int openConnections() {
        try (Statement statement = connection.createStatement()) {
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

        try {
            if (!cacheConnection && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LoggerFactory.getLogger(getClass().getName()).error("Could not close leak hunter connection", e);
        }
        if (openConnectionsAfterExecution > openConnectionsBeforeExecution) {
            throw new LeakHunterException(methodName, openConnectionsAfterExecution - openConnectionsBeforeExecution);
        }
    }

    protected abstract String leakCountSql();

}
