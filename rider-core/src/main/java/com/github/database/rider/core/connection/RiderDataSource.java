package com.github.database.rider.core.connection;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.filter.RiderPrimaryKeyFilter;
import com.github.database.rider.core.util.DriverUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConfig.ConfigProperty;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.db2.Db2DataTypeFactory;
import org.dbunit.ext.db2.Db2MetadataHandler;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author artemy-osipov
 */
public class RiderDataSource {

    public enum DBType {
        HSQLDB, H2, MYSQL, ORACLE, TIMESCALEDB, POSTGRESQL, DB2, MSSQL, UNKNOWN
    }

    private final ConnectionHolder connectionHolder;
    private final DBUnitConfig dbUnitConfig;
    private Connection connection; //the original test connection, it is created (or cached) on every test, internal usage only
    private DatabaseConnection dbUnitConnection;//connection used for dataset creation, db assertion, disabling constraints and cleaning db
    private DBType dbType;
    private Boolean autoCommit;

    public RiderDataSource(ConnectionHolder connectionHolder) {
        this(connectionHolder, DBUnitConfig.fromGlobalConfig());
    }

    public RiderDataSource(ConnectionHolder connectionHolder, DBUnitConfig dbUnitConfig) {
        this.connectionHolder = connectionHolder;
        this.dbUnitConfig = dbUnitConfig;
        try {
            init();
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database rider datasource.", e);
        }
    }

    public DatabaseConnection getDBUnitConnection() {
        return dbUnitConnection;
    }

    public DBType getDBType() {
        return dbType;
    }

    /**
     * Changes dbunit connection autoCommit
     *
     * @param autoCommit
     * @throws SQLException
     */
    public void setConnectionAutoCommit(Boolean autoCommit) throws SQLException {
        this.getDBUnitConnection().getConnection().setAutoCommit(autoCommit);
    }

    /**
     * Reset dbunit connection autoCommit
     *
     * @throws SQLException
     */
    public void resetConnectionAutoCommit() throws SQLException {
        this.getDBUnitConnection().getConnection().setAutoCommit(this.autoCommit);
    }

    private Connection getConnection() throws SQLException {
        if (!dbUnitConfig.isCacheConnection() || connection == null || connection.isClosed()) {
            connection = connectionHolder.getConnection();
        }
        return connection;
    }

    private void init() throws SQLException {
        final Connection conn = getConnection();
        if (conn != null) {
            checkDbType(conn);
            initDBUnitConnection(conn);
        }
    }

    private void initDBUnitConnection(final Connection connection) throws SQLException {
        try {
            dbUnitConnection = new DatabaseConnection(connection, dbUnitConfig.getSchema());
            autoCommit = connection.getAutoCommit();
            configDatabaseProperties();
        } catch (DatabaseUnitException e) {
            throw new SQLException(e);
        }
    }

    private void configDatabaseProperties() {
        DatabaseConfig config = dbUnitConnection.getConfig();
        for (Map.Entry<String, Object> p : dbUnitConfig.getProperties().entrySet()) {
            ConfigProperty byShortName = DatabaseConfig.findByShortName(p.getKey());
            if (byShortName != null) {
                Object propertyValue = p.getValue();
                if (propertyValue instanceof List) {
                    propertyValue = ((List) propertyValue).toArray(new String[((List) propertyValue).size()]);
                }
                config.setProperty(byShortName.getProperty(), propertyValue);
            }
        }

        if (!dbUnitConfig.getProperties().containsKey("datatypeFactory")) {
            IDataTypeFactory dataTypeFactory = getDataTypeFactory(dbType);
            if (dataTypeFactory != null) {
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory);
            }
        }
        if (!dbUnitConfig.getProperties().containsKey("metadataHandler")) {
            IMetadataHandler metadataHandler = getMetadataHandler(dbType);
            if (metadataHandler != null) {
                config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler);
            }
        }
        if (dbUnitConfig.getDisablePKCheckFor() != null && dbUnitConfig.getDisablePKCheckFor().length > 0) {
            config.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, new RiderPrimaryKeyFilter(Arrays.asList(dbUnitConfig.getDisablePKCheckFor())));
        }

    }

    private IDataTypeFactory getDataTypeFactory(DBType dbType) {
        switch (dbType) {
            case HSQLDB:
                return new HsqldbDataTypeFactory();
            case H2:
                return new H2DataTypeFactory();
            case MYSQL:
                return new MySqlDataTypeFactory();
            case POSTGRESQL:
                return new PostgresqlDataTypeFactory();
            case ORACLE:
                return new Oracle10DataTypeFactory();
            case MSSQL:
                return new MsSqlDataTypeFactory();
            case DB2:
                return new Db2DataTypeFactory();
            default:
                return null;
        }
    }

    private IMetadataHandler getMetadataHandler(DBType dbType) {
        switch (dbType) {
            case MYSQL:
                return new MySqlMetadataHandler();
            case DB2:
                return new Db2MetadataHandler();
            default:
                return null;
        }
    }

    private void checkDbType(Connection conn) throws SQLException {
        dbType = resolveDBType(conn);
        if (dbUnitConfig.getExpectedDbType() != DBType.UNKNOWN && dbUnitConfig.getExpectedDbType() != dbType) {
            throw new SQLException(String.format("Expect %s database, but actually %s database.",
                    dbUnitConfig.getExpectedDbType(), dbType));
        }
    }

    private DBType resolveDBType(Connection conn) throws SQLException {
        final DBType driverBasedType = resolveDBType(DriverUtils.getDriverName(conn));
        if (driverBasedType == DBType.POSTGRESQL && isTimescaleDb(conn)) {
            return DBType.TIMESCALEDB;
        }
        return driverBasedType;
    }

    private boolean isTimescaleDb(Connection conn) throws SQLException {
        final String schemaName = "_timescaledb_internal";
        final ResultSet schemas = conn.getMetaData().getSchemas();
        while (schemas.next()) {
            if (schemaName.equalsIgnoreCase(schemas.getString(1))) {
                return true;
            }
        }
        return false;
    }

    private DBType resolveDBType(String driverName) {
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
        } else if (DriverUtils.isDB2(driverName)) {
            return DBType.DB2;
        } else if (DriverUtils.isMsSql(driverName)) {
            return DBType.MSSQL;
        } else {
            return DBType.UNKNOWN;
        }
    }
}
