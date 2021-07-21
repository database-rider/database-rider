package com.github.database.rider.core.util;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.RESERVED_TABLE_NAMES;
import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.SYSTEM_SCHEMAS;

public final class TableNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TableNameResolver.class.getName());

    private final Set<String> tableNamesCache;

    private final DBUnitConfig dbUnitConfig;

    public TableNameResolver(DBUnitConfig dbUnitConfig) {
        if (dbUnitConfig == null) {
            dbUnitConfig = DBUnitConfig.fromGlobalConfig();
        }
        this.dbUnitConfig = dbUnitConfig;
        this.tableNamesCache = new HashSet<>();
    }

    public String resolveTableName(String name, RiderDataSource.DBType dbType) {
        if (RESERVED_TABLE_NAMES.contains(name.toLowerCase())) {
            name = escapeTableName(name, dbType);
        } else {
            // table name escaping may have been defined as well
            name = applyDBUnitEscapePattern(name);
        }
        return name;
    }

    public Set<String> getTableNames(RiderDataSource riderDataSource) {
        if (hasTableNamesCache()) {
            return tableNamesCache;
        }
        final Set<String> tables = new HashSet<>();
        try (ResultSet result = getTablesFromMetadata(riderDataSource.getConnection())) {
            while (result.next()) {
                String schema = resolveSchema(result);
                if (!isSystemSchema(schema, riderDataSource)) {
                    String name = result.getString("TABLE_NAME");
                    name = resolveTableName(name, riderDataSource.getDBType());
                    tables.add(schema != null && !"".equals(schema.trim()) ? schema + "." + name : name);
                }
            }
            tableNamesCache.addAll(tables);
            return tables;
        } catch (SQLException ex) {
            LOG.warn("An exception occurred while trying to analyse the database.", ex);
            return new HashSet<>();
        }
    }

    public boolean hasTableNamesCache() {
        return dbUnitConfig.isCacheTableNames() && !tableNamesCache.isEmpty();
    }

    public String resolveSchema(final Connection connection) {
        try {
            try (ResultSet tables = getTablesFromMetadata(connection)) {
                return resolveSchema(tables);
            }
        } catch (Exception e) {
            LOG.warn("Can't resolve schema", e);
            return dbUnitConfig.getSchema();
        }
    }

    private boolean isSystemSchema(String schema, RiderDataSource riderDataSource) {
        RiderDataSource.DBType dbType = riderDataSource.getDBType();
        Set<String> systemSchemas = SYSTEM_SCHEMAS.get(dbType);
        return systemSchemas != null && schema != null && systemSchemas.contains(schema.toUpperCase());
    }

    private ResultSet getTablesFromMetadata(Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        return metaData.getTables(null, null, "%", new String[]{"TABLE"});
    }

    private String resolveSchema(ResultSet result) {
        String schema = null;
        try {
            schema = result.getString("TABLE_SCHEM");
            if (schema == null) {
                schema = dbUnitConfig.getSchema();
            }
        } catch (Exception e) {
            LOG.warn("Can't resolve schema", e);
            schema = dbUnitConfig.getSchema();
        }
        return schema;
    }

    private String applyDBUnitEscapePattern(String table) {
        boolean hasEscapePattern = dbUnitConfig.getProperties().containsKey("escapePattern") && !"".equals(dbUnitConfig.getProperties().get("escapePattern").toString());
        if (hasEscapePattern) {
            String escapePattern = dbUnitConfig.getProperties().get("escapePattern").toString();
            if (table.contains(".")) {//skip schema and applies the pattern only on the table
                return table.substring(0, table.indexOf(".") + 1) + escapePattern.replace("?", table.substring(table.indexOf(".") + 1));
            } else {
                return escapePattern.replace("?", table);
            }
        } else {
            return table;
        }
    }

    private String escapeTableName(String name, RiderDataSource.DBType type) {
        switch (type) {
            case MSSQL:
                return "[" + name + "]";
            case HSQLDB:
            case H2:
            case DB2:
            case POSTGRESQL:
            case ORACLE:
                return "\"" + name + "\"";
            case MYSQL:
                return "`" + name + "`";
            default:
                return name;
        }
    }
}
