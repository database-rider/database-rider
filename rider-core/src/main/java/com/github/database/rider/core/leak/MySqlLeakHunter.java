package com.github.database.rider.core.leak;

import com.github.database.rider.core.api.leak.LeakHunter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by pestano on 07/09/16.
 */
class MySqlLeakHunter implements LeakHunter {


    private final String sql = "SELECT COUNT(*) FROM v$session WHERE status = 'INACTIVE'";

    Connection connection;

    public MySqlLeakHunter(Connection connection) {
        this.connection = connection;
    }


    @Override
    public int openConnections() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                    "SHOW PROCESSLIST")) {
                int count = 0;
                while (resultSet.next()) {
                    String state = resultSet.getString("command");
                    if ("sleep".equalsIgnoreCase(state)) {
                        count++;
                    }
                }
                return count;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
