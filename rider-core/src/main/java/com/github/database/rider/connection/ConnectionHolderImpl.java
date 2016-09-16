package com.github.database.rider.connection;

import com.github.database.rider.api.connection.ConnectionHolder;

import java.sql.Connection;

/**
 * Created by pestano on 25/07/15.
 */
public class ConnectionHolderImpl implements ConnectionHolder {

    private Connection connection;

    public ConnectionHolderImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
