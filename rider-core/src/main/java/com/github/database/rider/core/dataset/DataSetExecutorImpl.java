package com.github.database.rider.core.dataset;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.*;
import com.github.database.rider.core.assertion.DataSetAssertion;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.connection.RiderDataSource.DBType;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import com.github.database.rider.core.replacers.Replacer;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.PrimaryKeyFilteredTableWrapper;
import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.filter.SequenceTableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pestano on 26/07/15.
 */
public class DataSetExecutorImpl implements DataSetExecutor {

    public static final String DEFAULT_EXECUTOR_ID = "default";

    private static final Logger log = LoggerFactory.getLogger(DataSetExecutorImpl.class);

    private static final Map<String, DataSetExecutorImpl> executors = new ConcurrentHashMap<>();

    private static final String SEQUENCE_TABLE_NAME;

    private static final EnumMap<DBType, Set<String>> SYSTEM_SCHEMAS = new EnumMap<>(DBType.class);

    private static final List<String> RESERVED_TABLE_NAMES;

    private final AtomicBoolean printDBUnitConfig = new AtomicBoolean(true);

    private DBUnitConfig dbUnitConfig;

    private RiderDataSource riderDataSource;

    private ConnectionHolder connectionHolder;

    private final String executorId;

    private List<String> tableNames;

    private boolean isConstraintsDisabled = false;

    static {
        SEQUENCE_TABLE_NAME = System.getProperty("SEQUENCE_TABLE_NAME") == null ? "SEQ"
                : System.getProperty("SEQUENCE_TABLE_NAME");
        SYSTEM_SCHEMAS.put(DBType.MSSQL, Collections.singleton("SYS"));
        if (System.getProperty("RESERVED_TABLE_NAMES") != null) {
            RESERVED_TABLE_NAMES = Arrays.asList(System.getProperty("RESERVED_TABLE_NAMES").split(","));
        } else {
            RESERVED_TABLE_NAMES = Arrays.asList("user", "password", "value");
        }
    }

    public static DataSetExecutorImpl instance(ConnectionHolder connectionHolder) {
        // if no executor name is provided use default
        return instance(DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public static DataSetExecutorImpl instance(String executorId, ConnectionHolder connectionHolder) {
        DataSetExecutorImpl instance = executors.get(executorId);
        if (instance == null) {
            instance = new DataSetExecutorImpl(executorId, connectionHolder,
                    DBUnitConfig.fromGlobalConfig().executorId(executorId));
            log.debug("creating executor instance " + executorId);
            executors.put(executorId, instance);
        } else if (!instance.dbUnitConfig.isCacheConnection()) {
            instance.setConnectionHolder(connectionHolder);
        }

        return instance;
    }

    private DataSetExecutorImpl(String executorId, ConnectionHolder connectionHolder, DBUnitConfig dbUnitConfig) {
        this.connectionHolder = connectionHolder;
        this.executorId = executorId;
        this.dbUnitConfig = dbUnitConfig;
    }

    @Override
    public void createDataSet(DataSetConfig dataSetConfig) {
        if (printDBUnitConfig.compareAndSet(true, false)) {
            StringBuilder sb = new StringBuilder(150);
            sb.append("cacheConnection: ").append("" + dbUnitConfig.isCacheConnection()).append("\n")
                    .append("cacheTableNames: ").append(dbUnitConfig.isCacheTableNames()).append("\n")
                    .append("leakHunter: ").append("" + dbUnitConfig.isLeakHunter()).append("\n");

            for (Entry<String, Object> entry : dbUnitConfig.getProperties().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            log.info(String.format("DBUnit configuration for dataset executor '%s':\n" + sb.toString(),
                    this.executorId));
        }

        if (dataSetConfig != null) {
            IDataSet resultingDataSet = null;
            try {
                if (dataSetConfig.isDisableConstraints()) {
                    disableConstraints();
                }
                if (dataSetConfig.isCleanBefore()) {
                    try {
                        clearDatabase(dataSetConfig);
                    } catch (SQLException e) {
                        LoggerFactory.getLogger(DataSetExecutorImpl.class.getName())
                                .warn("Could not clean database before test.", e);
                    }
                }
                if (dataSetConfig.getExecuteStatementsBefore() != null
                        && dataSetConfig.getExecuteStatementsBefore().length > 0) {
                    executeStatements(dataSetConfig.getExecuteStatementsBefore());
                }
                if (dataSetConfig.getExecuteScriptsBefore() != null
                        && dataSetConfig.getExecuteScriptsBefore().length > 0) {
                    for (int i = 0; i < dataSetConfig.getExecuteScriptsBefore().length; i++) {
                        executeScript(dataSetConfig.getExecuteScriptsBefore()[i]);
                    }
                }
                if (dataSetConfig.hasDataSets() || dataSetConfig.hasDataSetProvider()) {
                    if (dataSetConfig.hasDataSets()) {
                        resultingDataSet = loadDataSets(dataSetConfig.getDatasets());
                    } else {
                        resultingDataSet = loadDataSetFromDataSetProvider(dataSetConfig.getProvider());
                        if (resultingDataSet == null) {
                            throw new RuntimeException("Provided dataset cannot be null. DataSet provider: " + dataSetConfig.getProvider().getName());
                        }
                    }
                    resultingDataSet = performSequenceFiltering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performTableOrdering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performReplacements(resultingDataSet);

                    DatabaseOperation operation = getOperation(dataSetConfig);

                    operation.execute(getRiderDataSource().getDBUnitConnection(), resultingDataSet);
                } else {
                    log.warn("Database will not be populated because no dataset has been provided.");
                }

            } catch (Exception e) {
                if (log.isDebugEnabled() && resultingDataSet != null) {
                    logDataSet(resultingDataSet, e);
                }
                throw new DataBaseSeedingException("Could not initialize dataset: " + dataSetConfig, e);
            }

        }
    }

    private void logDataSet(IDataSet resultingDataSet, Exception e) {
        try {
            File datasetFile = Files.createTempFile("dataset-log", ".yml").toFile();
            log.info("Saving current dataset to " + datasetFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(datasetFile)) {
                FlatXmlDataSet.write(resultingDataSet, fos);
            }
        } catch (Exception e1) {
            log.error("Could not log created dataset.", e);
        }
    }

    private IDataSet loadDataSetFromDataSetProvider(Class<? extends DataSetProvider> provider) {
        try {
            DataSetProvider dataSetProvider = provider.newInstance();
            return dataSetProvider.provide();
        } catch (Exception e) {
            throw new RuntimeException("Could not load dataset from provider: " + provider.getName(), e);
        }
    }

    private DatabaseOperation getOperation(DataSetConfig dataSetConfig) {
        SeedStrategy strategy = dataSetConfig.getstrategy();
        if (getRiderDataSource().getDBType() == RiderDataSource.DBType.MSSQL && dataSetConfig.isFillIdentityColumns()) {
            switch (strategy) {
                case INSERT:
                    return InsertIdentityOperation.INSERT;
                case REFRESH:
                    return InsertIdentityOperation.REFRESH;
                case CLEAN_INSERT:
                    return InsertIdentityOperation.CLEAN_INSERT;
                case TRUNCATE_INSERT:
                    return new CompositeOperation(DatabaseOperation.TRUNCATE_TABLE,
                            InsertIdentityOperation.INSERT);
            }
        }
        return strategy.getOperation();
    }

    /**
     * @param name one or more (comma separated) dataset names to instance
     * @return loaded dataset (in case of multiple dataSets they will be merged
     * in one using composite dataset)
     */
    @Override
    public IDataSet loadDataSet(String name) throws DataSetException, IOException {
        String[] dataSetNames = name.trim().split(",");

        List<IDataSet> dataSets = new ArrayList<>();
        for (String dataSet : dataSetNames) {
            IDataSet target = null;
            String dataSetName = dataSet.trim();
            String extension = dataSetName.substring(dataSetName.lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "yml": {
                    target = new ScriptableDataSet(new YamlDataSet(getDataSetStream(dataSetName), dbUnitConfig));
                    break;
                }
                case "xml": {
                    try {
                        target = new ScriptableDataSet(new FlatXmlDataSetBuilder().build(getDataSetUrl(dataSetName)));
                    } catch (Exception e) {
                        target = new ScriptableDataSet(new FlatXmlDataSetBuilder().build(getDataSetStream(dataSetName)));
                    }
                    break;
                }
                case "csv": {
                    target = new ScriptableDataSet(new CsvDataSet(
                            new File(getClass().getClassLoader().getResource(dataSetName).getFile()).getParentFile()));
                    break;
                }
                case "xls": {
                    target = new ScriptableDataSet(new XlsDataSet(getDataSetStream(dataSetName)));
                    break;
                }
                case "json": {
                    target = new ScriptableDataSet(new JSONDataSet(getDataSetStream(dataSetName)));
                    break;
                }
                default:
                    log.error("Unsupported dataset extension");
            }

            if (target != null) {
                dataSets.add(target);
            }
        }

        if (dataSets.isEmpty()) {
            throw new RuntimeException("No dataset loaded for name " + name);
        }

        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
    }

    private URL getDataSetUrl(String dataSet) {
        if (!dataSet.startsWith("/")) {
            dataSet = "/" + dataSet;
        }
        URL resource = getClass().getResource(dataSet);
        if (resource == null) {// if not found try to get from datasets folder
            resource = getClass().getResource("/datasets" + dataSet);
        }
        if (resource == null) {
            throw new RuntimeException(
                    String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",
                            dataSet.substring(1)));
        }
        return resource;
    }

    @Override
    public IDataSet loadDataSets(String[] datasets) throws DataSetException, IOException {
        List<IDataSet> dataSetList = new ArrayList<>();

        for (String name : datasets) {
            dataSetList.add(loadDataSet(name));
        }

        return new CompositeDataSet(dataSetList.toArray(new IDataSet[dataSetList.size()]));
    }

    private IDataSet performTableOrdering(DataSetConfig dataSet, IDataSet target) throws AmbiguousTableNameException {
        if (dataSet.getTableOrdering().length > 0) {
            target = new FilteredDataSet(new SequenceTableFilter(dataSet.getTableOrdering()), target);
        }
        return target;
    }

    private IDataSet performSequenceFiltering(DataSetConfig dataSet, IDataSet target)
            throws DatabaseUnitException, SQLException {
        if (dataSet.isUseSequenceFiltering()) {
            ITableFilter filteredTable = new DatabaseSequenceFilter(getRiderDataSource().getDBUnitConnection(),
                    target.getTableNames());
            target = new FilteredDataSet(filteredTable, target);
        }
        return target;
    }

    private void disableConstraints() throws SQLException {

        try (Statement statement = getRiderDataSource().getConnection().createStatement()) {
            switch (getRiderDataSource().getDBType()) {
                case HSQLDB:
                    statement.execute("SET DATABASE REFERENTIAL INTEGRITY FALSE;");
                    break;
                case H2:
                    statement.execute("SET foreign_key_checks = 0;");
                    break;
                case MYSQL:
                    statement.execute(" SET FOREIGN_KEY_CHECKS=0;");
                    break;
                case POSTGRESQL:
                    List<String> tables = getTableNames(getRiderDataSource().getConnection());
                    for (String tableName : tables) {
                        statement.execute("ALTER TABLE " + tableName + " DISABLE TRIGGER ALL;");
                    }
                    break;
                case ORACLE:
                    // adapted from Unitils:
                    // https://github.com/arteam/unitils/blob/master/unitils-core/src/main/java/org/unitils/core/dbsupport/OracleDbSupport.java#L190
                    ResultSet resultSet = null;
                    final String schemaName = resolveSchema();// default schema
                    try {
                        boolean hasSchema = schemaName != null && !"".equals(schemaName.trim());
                        // to be sure no recycled items are handled, all items with a name that starts with BIN$ will be
                        // filtered out.
                        resultSet = statement.executeQuery(
                                "select TABLE_NAME, CONSTRAINT_NAME from ALL_CONSTRAINTS where CONSTRAINT_TYPE = 'R' "
                                        + (hasSchema ? "and OWNER = '" + schemaName + "'" : "")
                                        + " and CONSTRAINT_NAME not like 'BIN$%' and STATUS <> 'DISABLED'");
                        while (resultSet.next()) {
                            String tableName = resultSet.getString("TABLE_NAME");
                            String constraintName = resultSet.getString("CONSTRAINT_NAME");
                            String qualifiedTableName = hasSchema ? "'" + schemaName + "'.'" + tableName + "'"
                                    : "'" + tableName + "'";
                            executeStatements(
                                    "alter table " + qualifiedTableName + " disable constraint '" + constraintName + "'");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error while disabling referential constraints on schema " + schemaName, e);
                    } finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                    break;
                case MSSQL:
                    tables = getTableNames(getRiderDataSource().getConnection());
                    for (String tableName : tables) {
                        statement.execute("alter table " + tableName + " nocheck constraint all");
                    }
                    break;
            }

        }

        isConstraintsDisabled = true;

    }

    @Override
    public void enableConstraints() throws SQLException {
        if (isConstraintsDisabled) {
            try (Statement statement = getRiderDataSource().getConnection().createStatement()) {
                switch (getRiderDataSource().getDBType()) {
                    case HSQLDB:
                        statement.execute("SET DATABASE REFERENTIAL INTEGRITY TRUE;");
                        break;
                    case H2:
                        statement.execute("SET foreign_key_checks = 1;");
                        break;
                    case MYSQL:
                        statement.execute(" SET FOREIGN_KEY_CHECKS=1;");
                        break;
                    case POSTGRESQL:
                        List<String> tables = getTableNames(getRiderDataSource().getConnection());
                        for (String tableName : tables) {
                            statement.execute("ALTER TABLE " + tableName + " ENABLE TRIGGER ALL;");
                        }
                        break;
                    case ORACLE:
                        // adapted from Unitils:
                        // https://github.com/arteam/unitils/blob/master/unitils-core/src/main/java/org/unitils/core/dbsupport/OracleDbSupport.java#L190
                        ResultSet resultSet = null;
                        final String schemaName = resolveSchema();// default schema
                        try {
                            boolean hasSchema = schemaName != null && !"".equals(schemaName.trim());
                            // to be sure no recycled items are handled, all items with a name that starts with BIN$ will be
                            // filtered out.
                            resultSet = statement.executeQuery(
                                    "select TABLE_NAME, CONSTRAINT_NAME from ALL_CONSTRAINTS where CONSTRAINT_TYPE = 'R' "
                                            + (hasSchema ? "and OWNER = '" + schemaName + "'" : "")
                                            + " and CONSTRAINT_NAME not like 'BIN$%' and STATUS = 'DISABLED'");
                            while (resultSet.next()) {
                                String tableName = resultSet.getString("TABLE_NAME");
                                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                                String qualifiedTableName = hasSchema ? "'" + schemaName + "'.'" + tableName + "'"
                                        : "'" + tableName + "'";
                                executeStatements(
                                        "alter table " + qualifiedTableName + " enable constraint '" + constraintName + "'");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Error while enabling referential constraints on schema " + schemaName,
                                    e);
                        } finally {
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        }
                        break;
                    case MSSQL:
                        tables = getTableNames(getRiderDataSource().getConnection());
                        for (String tableName : tables) {
                            statement.execute("alter table " + tableName + " with check check constraint all");
                        }
                        break;
                }

                isConstraintsDisabled = false;
            }

        }
    }

    @Override
    public void executeStatements(String... statements) {
        if (statements != null && statements.length > 0 && !"".equals(statements[0].trim())) {
            java.sql.Statement statement = null;
            try {
                boolean autoCommit = getRiderDataSource().getConnection().getAutoCommit();
                getRiderDataSource().getConnection().setAutoCommit(false);
                statement = getRiderDataSource().getConnection().createStatement(
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                for (String stm : statements) {
                    statement.addBatch(stm);
                }
                statement.executeBatch();
                getRiderDataSource().getConnection().commit();
                getRiderDataSource().getConnection().setAutoCommit(autoCommit);
            } catch (Exception e) {
                log.error("Could execute statements:" + e.getMessage(), e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ex) {
                        log.error("Could close statement.", ex);

                    }
                }
            }

        }
    }

    /**
     * Perform replacements from all {@link Replacer} implementations,
     * registered in {@link #dbUnitConfig}.
     */
    @SuppressWarnings("unchecked")
    private IDataSet performReplacements(IDataSet dataSet) {
        if (!dbUnitConfig.getProperties().containsKey("replacers")) {
            return dataSet;
        }
        return performReplacements(dataSet, (List<Replacer>) dbUnitConfig.getProperties().get("replacers"));
    }

    /**
     * Perform replacements from all {@link Replacer} implementations to given dataset
     * registered in {@link #dbUnitConfig}.
     */
    private IDataSet performReplacements(IDataSet dataSet, List<Replacer> replacersList) {

        if (replacersList == null || replacersList.isEmpty())
            return dataSet;

        ReplacementDataSet replacementSet = new ReplacementDataSet(dataSet);
        // convert to set to remove duplicates
        Set<Replacer> replacers = new HashSet<>((List<Replacer>) replacersList);
        for (Replacer replacer : replacers) {
            replacer.addReplacements(replacementSet);
        }

        return replacementSet;
    }

    private void setConnectionHolder(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
        riderDataSource = null;
    }

    @Override
    public void initConnectionFromConfig(final ConnectionConfig connectionConfig) {
        setConnectionHolder(new ConnectionHolder() {
            @Override
            public Connection getConnection() {
                return getConnectionFromConfig(connectionConfig);
            }
        });
    }

    private Connection getConnectionFromConfig(ConnectionConfig connectionConfig) {
        if ("".equals(connectionConfig.getUrl())) {
            throw new RuntimeException("JDBC connection url cannot be empty");
        }
        if (!"".equals(connectionConfig.getDriver())) {
            try {
                Class.forName(connectionConfig.getDriver());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return DriverManager.getConnection(connectionConfig.getUrl(), connectionConfig.getUser(),
                    connectionConfig.getPassword());
        } catch (SQLException e) {
            log.error("Could not create connection from configuration.", e);
            throw new RuntimeException("Could not create connection from configuration. See documentation here: https://github.com/database-rider/database-rider#jdbc-connection");
        }
    }

    public Connection getConnection() {
        try {
            return getRiderDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        return 17 * (executorId == null ? 0 : executorId.hashCode());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DataSetExecutorImpl)) {
            return false;
        }
        DataSetExecutorImpl otherExecutor = (DataSetExecutorImpl) other;
        if (executorId == null || otherExecutor.getExecutorId() == null) {
            return false;
        }
        return executorId.equals(otherExecutor.getExecutorId());
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    public static DataSetExecutorImpl getExecutorById(String id) {
        return executors.get(id);
    }

    private InputStream getDataSetStream(String dataSet) {
        if (!dataSet.startsWith("/")) {
            dataSet = "/" + dataSet;
        }
        InputStream is = getClass().getResourceAsStream(dataSet);
        if (is == null) {// if not found try to get from datasets folder
            is = getClass().getResourceAsStream("/datasets" + dataSet);
        }
        if (is == null) {
            throw new RuntimeException(
                    String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",
                            dataSet.substring(1)));
        }
        return is;
    }

    /**
     * @throws SQLException if clean up cannot be performed
     */
    @Override
    public void clearDatabase(DataSetConfig config) throws SQLException {
        Connection connection = getRiderDataSource().getConnection();
        if (config != null && config.getTableOrdering() != null && config.getTableOrdering().length > 0) {
            for (String table : config.getTableOrdering()) {
                if (table.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                    // tables containing 'SEQ'will NOT be cleared see
                    // https://github.com/rmpestano/dbunit-rules/issues/26
                    continue;
                }
                final String escapedTableName = getEscapedTableName(table);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM " + escapedTableName + " where 1=1");
                    connection.commit();
                } catch (Exception e) {
                    log.warn("Could not clear table " + escapedTableName + ", message:" + e.getMessage() + ", cause: "
                            + e.getCause());
                }
            }
        }
        // clear remaining tables in any order(if there are any, also no problem clearing again)
        List<String> tables = getTableNames(connection);
        List<String> tablesToSkipCleaning = config.getSkipCleaningFor() != null ? Arrays.asList(config.getSkipCleaningFor()) : Collections.<String>emptyList();
        for (String tableName : tables) {
            if(shouldSkipFromCleaning(tablesToSkipCleaning, tableName)) {
                continue;
            }
            if (tableName.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                // tables containing 'SEQ' will NOT be cleared see https://github.com/rmpestano/dbunit-rules/issues/26
                continue;
            }
            final String escapedTableName = getEscapedTableName(tableName);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM " + escapedTableName + " where 1=1");
                connection.commit();
            } catch (Exception e) {
                log.warn("Could not clear table " + escapedTableName + ", message:" + e.getMessage() + ", cause: "
                        + e.getCause());
            }
        }

    }

    private boolean shouldSkipFromCleaning(List<String> tablesToSkipCleaning, String tableName) {
        boolean skip = tablesToSkipCleaning.contains(tableName);
        if(!skip && tableName.contains(".")) {
            skip = tablesToSkipCleaning.contains(tableName.substring(tableName.indexOf(".")+1));
        }
        return skip;
    }

    private String getEscapedTableName(String table) {
        boolean hasEscapePattern = dbUnitConfig.getProperties().containsKey("escapePattern") && !"".equals(dbUnitConfig.getProperties().get("escapePattern").toString());
        if (hasEscapePattern) {
            String escapePattern = dbUnitConfig.getProperties().get("escapePattern").toString();
            if (table.contains(".")) {//skip schema and applies the pattern only on the table
                return table.substring(0, table.indexOf(".") + 1) + escapePattern.replace("?", table.substring(table.indexOf(".")+1));
            } else {
                return escapePattern.replace("?", table);
            }
        } else {
            return table;
        }
    }

    private List<String> getTableNames(Connection con) {
        if (tableNames != null && dbUnitConfig.isCacheTableNames()) {
            return tableNames;
        }
        List<String> tables = new ArrayList<String>();
        try (ResultSet result = getTablesFromMetadata(con)) {
            while (result.next()) {
                String schema = resolveSchema(result);
                if (!isSystemSchema(schema)) {
                    String name = result.getString("TABLE_NAME");
                    if (RESERVED_TABLE_NAMES.contains(name.toLowerCase())) {
                        name = escapeTableName(name);
                    }
                    tables.add(schema != null ? schema + "." + name : name);
                }
            }

            if (tableNames == null) {
                this.tableNames = new ArrayList<>(tables);
            }

            return tables;
        } catch (SQLException ex) {
            log.warn("An exception occured while trying to analyse the database.", ex);
            return new ArrayList<>();
        }
    }

    private String escapeTableName(String name) {
        switch (getRiderDataSource().getDBType()) {
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

    private boolean isSystemSchema(String schema) {
        DBType dbType = getRiderDataSource().getDBType();
        Set<String> systemSchemas = SYSTEM_SCHEMAS.get(dbType);
        return systemSchemas != null && schema != null && systemSchemas.contains(schema.toUpperCase());
    }

    private ResultSet getTablesFromMetadata(Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        return metaData.getTables(null, null, "%", new String[]{"TABLE"});
    }

    private String resolveSchema(ResultSet result) {
        try {
            return result.getString("TABLE_SCHEM");
        } catch (Exception e) {
            log.warn("Can't resolve schema", e);
            return null;
        }
    }

    private String resolveSchema() {
        try {
            try (ResultSet tables = getTablesFromMetadata(getRiderDataSource().getConnection())) {
                return resolveSchema(tables);
            }
        } catch (Exception e) {
            log.warn("Can't resolve schema", e);
            return null;
        }
    }

    @Override
    public void executeScript(String scriptPath) {
        if (scriptPath != null && !"".equals(scriptPath)) {
            if (!scriptPath.startsWith("/")) {
                scriptPath = "/" + scriptPath;
            }
            URL resource = getClass().getResource(scriptPath.trim());
            if (resource == null) {
                resource = getClass().getResource("/scripts" + scriptPath.trim());
            }
            if (resource == null) {
                throw new RuntimeException(String.format("Could not find script %s in classpath", scriptPath));
            }

            String[] scriptsStatements = readScriptStatements(resource);

            if (scriptsStatements != null && scriptsStatements.length > 0) {
                executeStatements(scriptsStatements);
            }
        }
    }

    String[] readScriptStatements(URL resource) {
        if ("jar".equals(resource.getProtocol())) {
            return readScriptStatementsFromJar(resource);
        }
        return readScriptStatementsFromFile(resource);
    }

    private String[] readScriptStatementsFromJar(URL resource) {
        String jarEntry = "jar:" + resource.getFile();
        JarURLConnection conn;
        InputStreamReader r = null;
        try {
            conn = (JarURLConnection) new URL(jarEntry).openConnection();
            r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            int data = r.read();
            while (data != -1) {
                sb.append((char) data);
                data = r.read();
            }
            List<String> statements = Arrays.asList(sb.toString().split(";"));
            List<String> result = new ArrayList<>();
            for (int i = 0; i < statements.size(); i++) {
                String trimmedStmt = statements.get(i).trim();
                if (!"".equals(trimmedStmt)) {
                    result.add(trimmedStmt);
                }
            }
            return result.toArray(new String[result.size()]);
        } catch (IOException e) {
            log.warn(String.format("Could not read script file %s.", jarEntry), e);
            return null;
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException e) {
                log.warn("Could not close script file " + jarEntry);
            }
        }
    }

    private String[] readScriptStatementsFromFile(URL resource) {
        File scriptFile = getFileFromURL(resource);
        if (scriptFile == null) return null;
        RandomAccessFile rad = null;
        int lineNum = 0;
        try {
            rad = new RandomAccessFile(scriptFile, "r");
            String line;
            List<String> scripts = new ArrayList<>();
            while ((line = rad.readLine()) != null) {
                // a line can have multiple scripts separated by ;
                String[] lineScripts = line.split(";");
                for (int i = 0; i < lineScripts.length; i++) {
                    String trimmedStmt = lineScripts[i].trim();
                    if (!"".equals(trimmedStmt)) {
                        scripts.add(trimmedStmt);
                    }
                }
                lineNum++;
            }
            return scripts.toArray(new String[scripts.size()]);
        } catch (Exception e) {
            log.warn(String.format("Could not read script file %s. Error in line %d.", scriptFile.getAbsolutePath(),
                    lineNum), e);
            return null;
        } finally {
            if (rad != null) {
                try {
                    rad.close();
                } catch (IOException e) {
                    log.warn("Could not close script file " + scriptFile.getAbsolutePath());

                }
            }
        }
    }

    private File getFileFromURL(URL resource) {
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            log.error(String.format("Could not read script file %s.", resource.getFile()), e);
            return null;
        }
    }

    @Override
    public void compareCurrentDataSetWith(DataSetConfig expectedDataSetConfig, String[] excludeCols,
                                          Class<? extends Replacer>[] replacers, String[] orderBy, CompareOperation compareOperation) throws DatabaseUnitException {
        IDataSet current = null;
        IDataSet expected = null;
        List<Replacer> expectedDataSetReplacers = new ArrayList<>();
        if (replacers != null && replacers.length > 0) {
            for (Class<? extends Replacer> replacerClass : replacers) {
                try {
                    expectedDataSetReplacers.add(replacerClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalArgumentException(replacerClass.getName() + " could not be instantiated as Replacer");
                }
            }
        }
        try {
            current = getRiderDataSource().getDBUnitConnection().createDataSet();
            if (expectedDataSetConfig.hasDataSetProvider()) {
                expected = loadDataSetFromDataSetProvider(expectedDataSetConfig.getProvider());
            } else if (expectedDataSetConfig.hasDataSets()) {
                expected = loadDataSets(expectedDataSetConfig.getDatasets());
            }
            if (expected == null) {
                throw new RuntimeException("Expected dataset was not provided.");
            }
            if (!expectedDataSetReplacers.isEmpty()) {
                expected = performReplacements(expected, expectedDataSetReplacers);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create dataset to compare.", e);
        }
        String[] tableNames = null;
        try {
            tableNames = expected.getTableNames();
        } catch (DataSetException e) {
            throw new RuntimeException("Could not extract dataset table names.", e);
        }

        for (String tableName : tableNames) {
            ITable expectedTable = null;
            ITable actualTable = null;
            try {
                expectedTable = expected.getTable(tableName);
                actualTable = current.getTable(tableName);
            } catch (DataSetException e) {
                throw new RuntimeException("DataSet comparison failed due to following exception: ", e);
            }
            if (orderBy != null && orderBy.length > 0) {
                List<String> validOrderByColumns = new ArrayList<>();//gather valid columns for sorting expected dataset
                for (int i = 0; i < expectedTable.getRowCount(); i++) {
                    for (String orderColumn : orderBy) {
                        try {
                            if (expectedTable.getValue(i, orderColumn).toString().startsWith("regex:")) {
                                throw new IllegalArgumentException(String.format("The orderBy column %s cannot use regex matching on table %s.",orderColumn, tableName));
                            }
                            validOrderByColumns.add(orderColumn);//add only existing columns on current table
                        } catch (NoSuchColumnException | NullPointerException ignored) {
                        }
                    }
                }
                expectedTable = new SortedTable(expectedTable, validOrderByColumns.toArray(new String[0]));
                actualTable = new SortedTable(actualTable, validOrderByColumns.toArray(new String[0]));
            }
            ITable filteredActualTable = DefaultColumnFilter.includedColumnsTable(actualTable,
                    expectedTable.getTableMetaData().getColumns());

            if (compareOperation == CompareOperation.CONTAINS) {
                filteredActualTable = filterTableByPrimaryKey(expectedTable, filteredActualTable);
            }

            DataSetAssertion.assertEqualsIgnoreCols(expectedTable, filteredActualTable, excludeCols);
        }

    }

    private ITable filterTableByPrimaryKey(ITable expectedDataSet, ITable actualDataSet) throws DataSetException {
        //PrimaryKeyFilteredTableWrapper uses Set.contains to check PK values, so types should be the same in both datasets
        DataType dataType = actualDataSet.getTableMetaData().getPrimaryKeys()[0].getDataType();
        String pkName = actualDataSet.getTableMetaData().getPrimaryKeys()[0].getColumnName();
        Set<Object> pkSet = new HashSet<>();
        for (int i = 0; i < expectedDataSet.getRowCount(); i++) {
            pkSet.add(dataType.typeCast(expectedDataSet.getValue(i, pkName)));
        }
        return new PrimaryKeyFilteredTableWrapper(actualDataSet, pkSet);
    }

    @Override
    public void compareCurrentDataSetWith(DataSetConfig expectedDataSetConfig, String[] excludeCols)
            throws DatabaseUnitException {
        compareCurrentDataSetWith(expectedDataSetConfig, excludeCols, null, null);
    }

    @Override
    public void compareCurrentDataSetWith(DataSetConfig expectedDataSetConfig, String[] excludeCols, Class<? extends Replacer>[] replacers, String[] orderBy) throws DatabaseUnitException {
        compareCurrentDataSetWith(expectedDataSetConfig, excludeCols, replacers, orderBy, CompareOperation.EQUALS);
    }

    @Override
    public void setDBUnitConfig(DBUnitConfig dbUnitConfig) {
        if (this.dbUnitConfig != dbUnitConfig) {
            this.dbUnitConfig = dbUnitConfig;
            riderDataSource = null;
        }
    }

    @Override
    public DBUnitConfig getDBUnitConfig() {
        return dbUnitConfig;
    }

    @Override
    public RiderDataSource getRiderDataSource() {
        if (riderDataSource == null) {
            if (connectionHolder == null) {
                initConnectionFromConfig(dbUnitConfig.getConnectionConfig());
            }
            riderDataSource = new RiderDataSource(connectionHolder, dbUnitConfig);
        }
        return riderDataSource;
    }

    public void clearRiderDataSource() {
        this.riderDataSource = null;
    }


}
