package com.github.database.rider.api.connection;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by pestano on 25/07/15.
 */
public interface ConnectionHolder extends Serializable{

    Connection getConnection() throws SQLException;

}
