package com.github.database.rider.core.connection;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.util.DriverUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class RiderDataSource {

    public enum DBType { HSQLDB, H2, MYSQL, ORACLE, POSTGRESQL, UNKNOWN }

    private final ConnectionHolder connectionHolder;
    private final DBUnitConfig dbUnitConfig;
    private Connection connection;
    private DatabaseConnection dbUnitConnection;
    private DBType dbType;

    public RiderDataSource(ConnectionHolder connectionHolder, DBUnitConfig dbUnitConfig) throws SQLException {
        this.connectionHolder = connectionHolder;
        this.dbUnitConfig = dbUnitConfig;
        init();
    }

    public Connection getConnection() throws SQLException {
        if (!dbUnitConfig.isCacheConnection() || connection == null || connection.isClosed()) {
            connection = connectionHolder.getConnection();
        }

        return connection;
    }

    public DatabaseConnection getDBUnitConnection() throws SQLException {
        if (!dbUnitConfig.isCacheConnection()) {
            initDBUnitConnection();
        }

        return dbUnitConnection;
    }

    public DBType getDBType() {
        return dbType;
    }

    private void init() throws SQLException {
        Connection conn = getConnection();
        if (conn != null) {
            dbType = resolveDBType(DriverUtils.getDriverName(conn));
            initDBUnitConnection();
        }
    }

    private void initDBUnitConnection() throws SQLException {
        try {
            dbUnitConnection = new DatabaseConnection(getConnection());
            configDatabaseProperties();
        } catch (DatabaseUnitException e) {
            throw new SQLException(e);
        }
    }

    private void configDatabaseProperties() throws SQLException {
        DatabaseConfig config = dbUnitConnection.getConfig();
        for (Map.Entry<String, Object> p : dbUnitConfig.getProperties().entrySet()) {
            config.setProperty(DatabaseConfig.findByShortName(p.getKey()).getProperty(), p.getValue());
        }

        switch (dbType) {
            case HSQLDB:
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
                break;
            case H2:
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
                break;
            case MYSQL:
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
                break;
            case POSTGRESQL:
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
                break;
            case ORACLE:
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
                break;
        }
    }

    private DBType resolveDBType(String driverName) throws SQLException {
        if (DriverUtils.isHsql(driverName)) {
            return DBType.HSQLDB;
        } else if (DriverUtils.isH2(driverName)) {
            return DBType.H2;
        } else if (DriverUtils.isMysql(driverName)) {
            return DBType.MYSQL;
        } else if (DriverUtils.isPostgre(driverName)) {
            return DBType.POSTGRESQL;
        } else if (DriverUtils.isOracle(driverName)) {
            return DBType.ORACLE;
        } else {
            return DBType.UNKNOWN;
        }
    }
}
