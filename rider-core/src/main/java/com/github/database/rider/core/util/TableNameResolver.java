package com.github.database.rider.core.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.RESERVED_TABLE_NAMES;
import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.SYSTEM_SCHEMAS;

public final class TableNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TableNameResolver.class.getName());
    private static final String TABLE_SCHEM = "TABLE_SCHEM";
    private static final String TABLE_CAT = "TABLE_CAT";

    private final Set<String> tableNamesCache;

    private final DBUnitConfig dbUnitConfig;

    public TableNameResolver() {
        this(DBUnitConfig.fromGlobalConfig());
    }

    public TableNameResolver(DBUnitConfig dbUnitConfig) {
        if (dbUnitConfig == null) {
            dbUnitConfig = DBUnitConfig.fromGlobalConfig();
        }
        this.dbUnitConfig = dbUnitConfig;
        this.tableNamesCache = new HashSet<>();
    }

    public String resolveTableName(String name, RiderDataSource riderDataSource) {
        if (RESERVED_TABLE_NAMES.contains(name.toUpperCase()) || getDatabaseReservedWords(riderDataSource).contains(name.toUpperCase())) {
            name = escapeTableName(name, riderDataSource);
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
        try (ResultSet result = getTablesFromMetadata(riderDataSource.getDBUnitConnection().getConnection())) {
            String schemaColumnLabel = getSchemaColumnLabel(getDatabaseMetaData(riderDataSource));

            while (result.next()) {
                String schema = resolveSchema(result, schemaColumnLabel);
                if (!isSystemSchema(schema, riderDataSource) && !skipSchema(schema)) {
                    String name = result.getString("TABLE_NAME");
                    name = resolveTableName(name, riderDataSource);
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

    public String resolveSchema(final Connection connection) {
        try {
            String schemaColumnLabel = getSchemaColumnLabel(connection.getMetaData());
            try (ResultSet tables = getTablesFromMetadata(connection)) {
                return resolveSchema(tables, schemaColumnLabel);
            }
        } catch (Exception e) {
            LOG.warn("Can't resolve schema", e);
            return dbUnitConfig.getSchema();
        }
    }

    private String getSchemaColumnLabel(DatabaseMetaData databaseMetaData) throws SQLException{
        return databaseMetaData.getSchemaTerm() == null || databaseMetaData.getSchemaTerm().isEmpty() ?
                TABLE_CAT : TABLE_SCHEM;
    }

    private boolean hasTableNamesCache() {
        return dbUnitConfig.isCacheTableNames() && !tableNamesCache.isEmpty();
    }

    private boolean isSystemSchema(String schema, RiderDataSource riderDataSource) {
        RiderDataSource.DBType dbType = riderDataSource.getDBType();
        Set<String> systemSchemas = SYSTEM_SCHEMAS.get(dbType);
        return systemSchemas != null && schema != null && systemSchemas.contains(schema.toUpperCase());
    }

    private boolean skipSchema(String schema) {
        return schema != null &&
            dbUnitConfig.getSkipSchemas() != null &&
            Arrays.asList(dbUnitConfig.getSkipSchemas()).contains(schema);
    }

    private ResultSet getTablesFromMetadata(Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        return metaData.getTables(null, null, "%", new String[]{"TABLE"});
    }

    private String resolveSchema(ResultSet result, String columnLabel) {
        String schema = null;
        try {
            schema = result.getString(columnLabel);
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
                return table.substring(0, table.indexOf(".") + 1) + formatTableName(table.substring(table.indexOf(".") + 1), escapePattern);
            } else {
                return formatTableName(table, escapePattern);
            }
        } else {
            return table;
        }
    }

    private String formatTableName(String tableName, String escapePattern) {
        return escapePattern.contains("?") ? escapePattern.replace("?", tableName) : String.format("%s%s%s", escapePattern, tableName, escapePattern);
    }

    private DatabaseMetaData getDatabaseMetaData(RiderDataSource riderDataSource) {
        try {
            return getDatabaseMetaData(riderDataSource.getDBUnitConnection().getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve database metadata: " + e.getMessage(), e);
        }
    }

    private DatabaseMetaData getDatabaseMetaData(Connection connection) {
        try {
            return connection.getMetaData();
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve database metadata: " + e.getMessage(), e);
        }
    }

    private Set<String> getDatabaseReservedWords(RiderDataSource riderDataSource) {
        HashSet<String> databaseReservedWords = new HashSet<>();
        try {
            Collections.addAll(databaseReservedWords, getDatabaseMetaData(riderDataSource).getSQLKeywords().toUpperCase().split(","));
        } catch (SQLException e) {
            LOG.warn("Unable to get SQL keywords", e);
        }
        return databaseReservedWords;
    }

    private String getDatabaseEscapePattern(RiderDataSource riderDataSource) {
        try {
            return getDatabaseMetaData(riderDataSource).getIdentifierQuoteString().trim();
        } catch (SQLException e) {
            LOG.warn("Unable to get database escape pattern, will use empty string", e);
            return "";
        }
    }

    private String escapeTableName(String name, RiderDataSource riderDataSource) {
        String escapePattern = getDatabaseEscapePattern(riderDataSource);
        return formatTableName(name, escapePattern);
    }
}
