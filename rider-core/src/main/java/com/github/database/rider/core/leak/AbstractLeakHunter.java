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

    @Override
    public int openConnections() {
        try (Statement statement = getConnection().createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(leakCountSql())) {
                while (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract String leakCountSql();

    protected abstract Connection getConnection();

}
