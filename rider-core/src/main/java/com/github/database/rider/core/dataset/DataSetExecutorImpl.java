package com.github.database.rider.core.dataset;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.*;
import com.github.database.rider.core.assertion.DataSetAssertion;
import com.github.database.rider.core.assertion.PrologAssert;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import com.github.database.rider.core.replacers.Replacer;
import com.github.database.rider.core.util.ContainsFilterTable;
import com.github.database.rider.core.util.TableNameResolver;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSet;
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.DATASETS_FOLDER;
import static com.github.database.rider.core.configuration.DBUnitConfig.Constants.SEQUENCE_TABLE_NAME;

/**
 * Created by pestano on 26/07/15.
 */
public class DataSetExecutorImpl implements DataSetExecutor {

    public static final String DEFAULT_EXECUTOR_ID = "default";

    private static final Logger log = LoggerFactory.getLogger(DataSetExecutorImpl.class);

    private static final Map<String, DataSetExecutorImpl> executors = new ConcurrentHashMap<>();

    private final AtomicBoolean printDBUnitConfig = new AtomicBoolean(true);

    private DBUnitConfig dbUnitConfig;

    private RiderDataSource riderDataSource;

    private ConnectionHolder connectionHolder;

    private final String executorId;

    private boolean isConstraintsDisabled = false;

    private TableNameResolver tableNameResolver;

    public static DataSetExecutorImpl instance(ConnectionHolder connectionHolder) {
        // if no executor name is provided use default
        return instance(DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public static DataSetExecutorImpl instance(String executorId, ConnectionHolder connectionHolder) {
        return instance(executorId, connectionHolder, DBUnitConfig.fromGlobalConfig().executorId(executorId));
    }

    public static DataSetExecutorImpl instance(String executorId, ConnectionHolder connectionHolder, DBUnitConfig dbUnitConfig) {
        DataSetExecutorImpl instance = executors.get(executorId);
        if (instance == null) {
            instance = new DataSetExecutorImpl(executorId, connectionHolder, dbUnitConfig);
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
        this.tableNameResolver = new TableNameResolver(dbUnitConfig);
    }

    @Override
    public void createDataSet(DataSetConfig dataSetConfig) {
        printDBUnitConfiguration();
        if (dataSetConfig != null) {
            IDataSet resultingDataSet = null;
            try {
                if (dataSetConfig.isDisableConstraints()) {
                    disableConstraints();
                }
                if (dbUnitConfig.isAlwaysCleanBefore() || dataSetConfig.isCleanBefore()) {
                    clearDatabase(dataSetConfig);
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
                    resultingDataSet = performReplacements(resultingDataSet, getReplacerInstances(dataSetConfig.getReplacers()));
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

    private boolean isSequenceFilteringEnabled(DBUnitConfig dbUnitConfig, DataSetConfig dataSetConfig) {
        if (dbUnitConfig.isDisableSequenceFiltering()) {
            return false;
        }
        return dataSetConfig.isUseSequenceFiltering();
    }

    private void printDBUnitConfiguration() {
        if (printDBUnitConfig.compareAndSet(true, false)) {
            StringBuilder sb = new StringBuilder(150);
            sb.append("cacheConnection: ").append("" + dbUnitConfig.isCacheConnection()).append("\n")
                    .append("cacheTableNames: ").append(dbUnitConfig.isCacheTableNames()).append("\n")
                    .append("caseInsensitiveStrategy: ").append(dbUnitConfig.getCaseInsensitiveStrategy()).append("\n")
                    .append("columnSensing: ").append("" + dbUnitConfig.isColumnSensing()).append("\n")
                    .append("leakHunter: ").append("" + dbUnitConfig.isLeakHunter()).append("\n")
                    .append("mergeDataSets: ").append(dbUnitConfig.isMergeDataSets()).append("\n")
                    .append("mergingStrategy: ").append(dbUnitConfig.getMergingStrategy()).append("\n")
                    .append("disableSequenceFiltering: ").append(dbUnitConfig.isDisableSequenceFiltering()).append("\n")
                    .append("alwaysCleanBefore: ").append(dbUnitConfig.isAlwaysCleanBefore()).append("\n")
                    .append("alwaysCleanAfter: ").append(dbUnitConfig.isAlwaysCleanAfter()).append("\n")
                    .append("raiseExceptionOnCleanUp: ").append(dbUnitConfig.isRaiseExceptionOnCleanUp()).append("\n")
                    .append("disablePKCheckFor: ").append("" + dbUnitConfig.disablePKCheckFor()).append("\n");

            for (Entry<String, Object> entry : dbUnitConfig.getProperties().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue() == null ? "" : entry.getValue()).append("\n");
            }
            log.info("DBUnit configuration for dataset executor '{}':{}", this.executorId, sb.toString());
        }
    }

    private void logDataSet(IDataSet resultingDataSet, Exception e) {
        try {
            File datasetFile = Files.createTempFile("dataset-log", ".xml").toFile();
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
            DataSetProvider dataSetProvider = provider.getDeclaredConstructor().newInstance();
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
        final String[] dataSetNames = name.trim().split(",");
        List<IDataSet> dataSets = new ArrayList<>();
        final boolean sensitiveTableNames = dbUnitConfig.isCaseSensitiveTableNames();
        for (String dataSet : dataSetNames) {
            IDataSet target = null;
            String dataSetName = dataSet.trim();
            String extension = dataSetName.substring(dataSetName.lastIndexOf('.') + 1).toLowerCase();
            URL dataSetUrl = getDataSetUrl(dataSetName);
            InputStream datasetStream = null;
            switch (extension) {
                case "yaml":
                case "yml": {
                    datasetStream = getDataSetStream(dataSetUrl);
                    target = new ScriptableDataSet(sensitiveTableNames, new YamlDataSet(datasetStream, dbUnitConfig));
                    break;
                }
                case "xml": {
                    datasetStream = getDataSetStream(dataSetUrl);
                    target = new ScriptableDataSet(sensitiveTableNames, new FlatXmlDataSetBuilder()
                            .setColumnSensing(dbUnitConfig.isColumnSensing())
                            .setCaseSensitiveTableNames(sensitiveTableNames)
                            .build(datasetStream));
                    break;
                }
                case "csv": {
                    if (!dataSetUrl.getProtocol().equals("file")) {
                        throw new RuntimeException("Csv datasets can only be accessed from files");
                    }
                    target = new ScriptableDataSet(sensitiveTableNames, new CsvDataSet(
                            new File(dataSetUrl.getPath()).getParentFile()));
                    break;
                }
                case "xls": {
                    datasetStream = getDataSetStream(dataSetUrl);
                    target = new ScriptableDataSet(sensitiveTableNames, new XlsDataSet(datasetStream));
                    break;
                }
                case "json": {
                    datasetStream = getDataSetStream(dataSetUrl);
                    target = new ScriptableDataSet(sensitiveTableNames, new JSONDataSet(datasetStream));
                    break;
                }
                default:
                    log.error("Unsupported dataset extension");
            }
            if (target != null) {
                dataSets.add(target);
            }
            if (datasetStream != null) {
                datasetStream.close();
            }
        }
        if (dataSets.isEmpty()) {
            throw new RuntimeException("No dataset loaded for name " + name);
        }

        return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]), true, sensitiveTableNames);
    }

    @Override
    public IDataSet loadDataSets(String[] datasets) throws DataSetException, IOException {
        List<IDataSet> dataSetList = new ArrayList<>();
        for (String name : datasets) {
            dataSetList.add(loadDataSet(name));
        }
        return new CompositeDataSet(dataSetList.toArray(new IDataSet[dataSetList.size()]));
    }

    private IDataSet performTableOrdering(DataSetConfig dataSet, IDataSet target) throws DataSetException {
        if (dataSet.getTableOrdering().length > 0) {
            final IDataSet tableOrderingDataset = new FilteredDataSet(new SequenceTableFilter(dataSet.getTableOrdering(), dbUnitConfig.isCaseSensitiveTableNames()), target);
            final List<String> tablesNotDeclaredInOrdering = getTablesNotPresentInOrdering(target, dataSet);
            if (!tablesNotDeclaredInOrdering.isEmpty()) {
                final IDataSet unorderedDataSet = new FilteredDataSet(new SequenceTableFilter(tablesNotDeclaredInOrdering.toArray(new String[tablesNotDeclaredInOrdering.size()]), dbUnitConfig.isCaseSensitiveTableNames()), target);
                target = new CompositeDataSet(unorderedDataSet, tableOrderingDataset);
            } else {
                target = new FilteredDataSet(new SequenceTableFilter(dataSet.getTableOrdering(), dbUnitConfig.isCaseSensitiveTableNames()), target);
            }
        }
        return target;
    }

    private List<String> getTablesNotPresentInOrdering(IDataSet target, DataSetConfig dataSetConfig) throws DataSetException {
        List<String> tablesNotPresentInOrdering = new ArrayList<>();
        final List<String> tableOrderingList = Arrays.asList(dataSetConfig.getTableOrdering());
        for (String table : target.getTableNames()) {
            if (!tableOrderingList.contains(table) && !tableOrderingList.contains(table.toLowerCase())
                    && !tableOrderingList.contains(table.toUpperCase())) {
                tablesNotPresentInOrdering.add(table);
            }
        }
        return tablesNotPresentInOrdering;
    }

    private IDataSet performSequenceFiltering(DataSetConfig dataSet, IDataSet target)
            throws DatabaseUnitException, SQLException {
        boolean sequenceFilteringEnabled = isSequenceFilteringEnabled(dbUnitConfig, dataSet);
        if (sequenceFilteringEnabled) {
            ITableFilter filteredTable = new RiderSequenceFilter(getRiderDataSource().getDBUnitConnection(),
                    target.getTableNames(), dbUnitConfig);
            target = new FilteredDataSet(filteredTable, target);
        }
        return target;
    }

    private void disableConstraints() throws SQLException {
        if (!isConstraintsDisabled) {
            handleConstraints(false);
        }
    }

    @Override
    public void enableConstraints() throws SQLException {
        if (isConstraintsDisabled) {
            handleConstraints(true);
        }
    }

    public void handleConstraints(boolean enable) throws SQLException {
        try (Statement statement = getRiderDataSource().getDBUnitConnection().getConnection().createStatement()) {
            switch (getRiderDataSource().getDBType()) {
                case HSQLDB:
                    String hsqlDBFlag = enable ? "TRUE" : "FALSE";
                    executeStatements("SET DATABASE REFERENTIAL INTEGRITY " + hsqlDBFlag + ";");
                    break;
                case H2:
                    String h2DBFlag = enable ? "TRUE" : "FALSE";
                    executeStatements("SET REFERENTIAL_INTEGRITY " + h2DBFlag + ";");
                    break;
                case MYSQL:
                    String mySqlDBFlag = enable ? "1" : "0";
                    executeStatements(" SET FOREIGN_KEY_CHECKS=" + mySqlDBFlag + ";");
                    break;
                case POSTGRESQL:
                    String postgreSqlDBFlag = enable ? "DEFAULT" : "replica";
                    executeStatements("SET session_replication_role = " + postgreSqlDBFlag + ";");
                    break;
                case ORACLE:
                    // adapted from Unitils:
                    // https://github.com/arteam/unitils/blob/master/unitils-core/src/main/java/org/unitils/core/dbsupport/OracleDbSupport.java#L190
                    ResultSet resultSet = null;
                    final String schemaName = tableNameResolver.resolveSchema(getRiderDataSource().getDBUnitConnection().getConnection());// default schema
                    String enableDisable = enable ? "enable" : "disable";
                    try {
                        boolean hasSchema = schemaName != null && !"".equals(schemaName.trim());
                        // to be sure no recycled items are handled, all items with a name that starts with BIN$ will be
                        // filtered out.
                        String oracleSqlQueryCondition = enable ? "=" : "<>";
                        resultSet = statement.executeQuery(
                                "select TABLE_NAME, CONSTRAINT_NAME from ALL_CONSTRAINTS where CONSTRAINT_TYPE = 'R' " + (
                                        hasSchema ?
                                                "and OWNER = '" + schemaName + "'" :
                                                "") + " and CONSTRAINT_NAME not like 'BIN$%' and STATUS "
                                        + oracleSqlQueryCondition + " 'DISABLED'");
                        while (resultSet.next()) {
                            String tableName = resultSet.getString("TABLE_NAME");
                            String escapedTableName = tableNameResolver.resolveTableName(tableName, getRiderDataSource());
                            String constraintName = resultSet.getString("CONSTRAINT_NAME");
                            String qualifiedTableName = hasSchema ? schemaName + "." + escapedTableName : escapedTableName;

                            executeStatements("alter table " + qualifiedTableName + " " + enableDisable + " constraint "
                                    + constraintName);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Error while " + enableDisable + " referential constraints on schema " + schemaName, e);
                    } finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                    break;
                case MSSQL:
                    String msSqlDBFlag = enable ? "with check check" : "nocheck";
                    Set<String> tables = tableNameResolver.getTableNames(getRiderDataSource());
                    for (String tableName : tables) {
                        executeStatements("alter table " + tableName + " " + msSqlDBFlag + " constraint all");
                    }
                    break;
            }
            isConstraintsDisabled = !isConstraintsDisabled;
        }
    }

    @Override
    public void executeStatements(String... statements) {
        if (statements != null && statements.length > 0 && !"".equals(statements[0].trim())) {
            java.sql.Statement statement = null;
            try {
                final Connection connection = getRiderDataSource().getDBUnitConnection().getConnection();
                getRiderDataSource().setConnectionAutoCommit(false);
                statement = connection.createStatement(
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                for (String stm : statements) {
                    statement.addBatch(stm);
                }
                statement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                throw new RuntimeException("Could not execute statements:" + e.getMessage(), e);
            } finally {
                try {
                    getRiderDataSource().resetConnectionAutoCommit();
                } catch (SQLException e) {
                    log.error("Could not reset connection auto commit", e);
                }
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
     * Perform replacements from all {@link Replacer} implementations to given dataset
     * registered in {@link #dbUnitConfig}.
     */
    private IDataSet performReplacements(IDataSet dataSet, List<Replacer> replacersList) {

        if (replacersList == null || replacersList.isEmpty()) {
            //try to get replacers from global config
            if (dbUnitConfig.getProperties().containsKey("replacers")) {
                replacersList = (List<Replacer>) dbUnitConfig.getProperties().get("replacers");
                if (replacersList == null || replacersList.isEmpty()) {
                    return dataSet;
                }
            }
        }
        ReplacementDataSet replacementSet = new ReplacementDataSet(dataSet);
        // convert to set to remove duplicates
        Set<Replacer> replacers = new HashSet<>(replacersList);
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

    /**
     * @deprecated Use <code>getRiderDataSource().getDBUnitConnection().getConnection()</code>
     */
    @Deprecated
    public Connection getConnection() {
        try {
            return getRiderDataSource().getDBUnitConnection().getConnection();
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

    private URL getDataSetUrl(String dataSet) {
        URL url;
        final String dataSetWithoutLeadingSlash = dataSet.startsWith("/") ? dataSet.substring(1) : dataSet;
        url = Thread.currentThread().getContextClassLoader().getResource(dataSetWithoutLeadingSlash);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(DATASETS_FOLDER + dataSetWithoutLeadingSlash);
        }
        if (url == null) {
            try {
                url = new URL(dataSet);
            } catch (MalformedURLException e) {
            }
        }
        if (url == null) {
            throw new RuntimeException(String.format("The dataset '%s' is neither a valid URL nor could be found under 'resources' or 'resources/%s' directory.", dataSet, DATASETS_FOLDER));
        }
        return url;
    }

    private InputStream getDataSetStream(URL url) {
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (IOException e) {
            throw new RuntimeException(String.format("The dataset '%s' cannot be accessed", url.getPath()));
        }
        return inputStream;
    }

    /**
     * @throws SQLException if clean up cannot be performed
     */
    @Override
    public void clearDatabase(DataSetConfig config) throws SQLException {
        try {
            disableConstraints();
            List<String> cleanupStatements = new LinkedList<>();
            if (config != null && config.getTableOrdering() != null && config.getTableOrdering().length > 0) {
                for (String table : config.getTableOrdering()) {
                    if (table.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                        // tables containing 'SEQ'will NOT be cleared see
                        // https://github.com/rmpestano/dbunit-rules/issues/26
                        continue;
                    }
                    final String escapedTableName = tableNameResolver.resolveTableName(table, getRiderDataSource());
                    cleanupStatements.add("DELETE FROM " + escapedTableName + " where 1=1");
                }
            }
            // clear remaining tables in any order(if there are any, also no problem clearing again)
            final Set<String> tables = tableNameResolver.getTableNames(getRiderDataSource());
            final List<String> tablesToSkipCleaning = getTablesToSkipOnCleaning(config);
            for (String tableName : tables) {
                if (shouldSkipFromCleaning(tablesToSkipCleaning, tableName)) {
                    continue;
                }
                if (tableName.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                    // tables containing 'SEQ' will NOT be cleared see https://github.com/rmpestano/dbunit-rules/issues/26
                    continue;
                }
                cleanupStatements.add("DELETE FROM " + tableName + " where 1=1");
            }
            if (!cleanupStatements.isEmpty()) {
                executeStatements(cleanupStatements.toArray(new String[cleanupStatements.size()]));
            }
        } catch (Exception e) {
            if (dbUnitConfig.isRaiseExceptionOnCleanUp()) {
                throw new RuntimeException("Could not clean database before test.", e);
            }
            log.warn("Could not clean database before test.", e);
        } finally {
            // enabling constraints only if `disableConstraints == false`
            if (!config.isDisableConstraints()) {
                enableConstraints();
            }
        }
    }

    private List<String> getTablesToSkipOnCleaning(DataSetConfig config) {
        List<String> tablesToSkipOnCleaning = config.getSkipCleaningFor() != null ? Arrays.asList(config.getSkipCleaningFor()) : Collections.<String>emptyList();
        if (!tablesToSkipOnCleaning.isEmpty()) {
            for (Iterator<String> it = tablesToSkipOnCleaning.iterator(); it.hasNext(); ) {
                String tableName = it.next();
                tablesToSkipOnCleaning.set(tablesToSkipOnCleaning.indexOf(tableName), tableNameResolver.resolveTableName(tableName, getRiderDataSource()));
            }
        }
        return tablesToSkipOnCleaning;
    }

    private boolean shouldSkipFromCleaning(List<String> tablesToSkipCleaning, String tableName) {
        boolean skip = tablesToSkipCleaning.contains(tableName);
        if (!skip && tableName.contains(".")) {
            skip = tablesToSkipCleaning.contains(tableName.substring(tableName.indexOf(".") + 1));
        }
        return skip;
    }

    @Override
    public void executeScript(String scriptPath) {
        if (scriptPath != null && !"".equals(scriptPath)) {
            final String scriptPathWithoutLeadingSlash = scriptPath.startsWith("/") ? scriptPath.substring(1) : scriptPath;
            URL resource = Thread.currentThread().getContextClassLoader().getResource(scriptPathWithoutLeadingSlash.trim());
            if (resource == null) {
                resource = Thread.currentThread().getContextClassLoader().getResource("scripts/" + scriptPathWithoutLeadingSlash.trim());
            }
            if (resource == null) {
                throw new RuntimeException(String.format("Could not find script %s in classpath", scriptPath));
            }
            final String[] scriptsStatements = readScriptStatements(resource);
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
        InputStreamReader r = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new URL(jarEntry).openConnection().getInputStream(),
                        StandardCharsets.UTF_8
                ))
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            List<String> statements = splitScript(sb.toString());
            return statements.toArray(new String[statements.size()]);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not read script file %s.", jarEntry), e);
        }
    }

    private String[] readScriptStatementsFromFile(URL resource) {
        File scriptFile = getFileFromURL(resource);
        if (scriptFile == null) return null;
        int lineNum = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
                lineNum++;
            }
            List<String> statements = splitScript(sb.toString());
            return statements.toArray(new String[statements.size()]);
        } catch (Exception e) {
            throw new RuntimeException(String.format(String.format("Could not read script file %s. Error in line %d.", scriptFile.getAbsolutePath(),
                    lineNum), e));
        }
    }

    /**
     * Place to handle database difference
     */
    private List<String> splitScript(String script) {
        String separator = ";";
        String[] commentPrefixes = new String[]{"#", "--"};
        String blockCommentStartDelimiter = "/*";
        String blockCommentEndDelimiter = "*/";

        return splitScript(script, separator, commentPrefixes, blockCommentStartDelimiter, blockCommentEndDelimiter);
    }

    /**
     * Code below Modified from Spring org.springframework.jdbc.datasource.init.ScriptUtils
     * Support multiple commentPrefixes
     */
    private List<String> splitScript(String script, String separator, String[] commentPrefixes, String blockCommentStartDelimiter, String blockCommentEndDelimiter) {
        List<String> statements = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inEscape = false;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            if (inEscape) {
                inEscape = false;
                sb.append(c);
                continue;
            }
            // MySQL style escapes
            if (c == '\\') {
                inEscape = true;
                sb.append(c);
                continue;
            }
            if (!inDoubleQuote && (c == '\'')) {
                inSingleQuote = !inSingleQuote;
            } else if (!inSingleQuote && (c == '"')) {
                inDoubleQuote = !inDoubleQuote;
            }
            if (!inSingleQuote && !inDoubleQuote) {
                if (script.startsWith(separator, i)) {
                    // We've reached the end of the current statement
                    if (sb.length() > 0) {
                        statements.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    continue;
                } else {
                    boolean startWithCommentPrefix = false;
                    for (String commentPrefix : commentPrefixes) {
                        startWithCommentPrefix |= script.startsWith(commentPrefix, i);
                    }
                    if (startWithCommentPrefix) {
                        // Skip over any content from the start of the comment to the EOL
                        int indexOfNextNewline = script.indexOf("\n", i);
                        if (indexOfNextNewline > i) {
                            i = indexOfNextNewline;
                            continue;
                        } else {
                            // If there's no EOL, we must be at the end of the script, so stop here.
                            break;
                        }
                    } else if (script.startsWith(blockCommentStartDelimiter, i)) {
                        // Skip over any block comments
                        int indexOfCommentEnd = script.indexOf(blockCommentEndDelimiter, i);
                        if (indexOfCommentEnd > i) {
                            i = indexOfCommentEnd + blockCommentEndDelimiter.length() - 1;
                            continue;
                        } else {
                            throw new RuntimeException("Missing block comment end delimiter: " + blockCommentEndDelimiter);
                        }
                    } else if (c == ' ' || c == '\n' || c == '\t') {
                        // Avoid multiple adjacent whitespace characters
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            c = ' ';
                        } else {
                            continue;
                        }
                    }
                }
            }
            sb.append(c);
        }
        String lastString = sb.toString().trim();
        if (lastString.length() > 0) {
            statements.add(lastString);
        }
        return statements;
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
        List<Replacer> expectedDataSetReplacers = getReplacerInstances(replacers);
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
            expected = performReplacements(expected, expectedDataSetReplacers);
        } catch (Exception e) {
            throw new RuntimeException("Could not create dataset to compare.", e);
        }
        String[] tableNames = null;
        try {
            tableNames = expected.getTableNames();
        } catch (DataSetException e) {
            throw new RuntimeException("Could not extract dataset table names.", e);
        }

        switch (compareOperation) {
            case PROLOG:
                PrologAssert.compareProlog(current, expected, tableNames, dbUnitConfig.getPrologTimeout());
                break;
            case EQUALS:
            case CONTAINS:
                compareClassic(excludeCols, orderBy, compareOperation, current, expected, tableNames);
                break;
        }
    }

    private void compareClassic(String[] excludeCols, String[] orderBy, CompareOperation compareOperation, IDataSet current, IDataSet expected, String[] tableNames) throws DatabaseUnitException {
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
                                throw new IllegalArgumentException(String.format("The orderBy column %s cannot use regex matching on table %s.", orderColumn, tableName));
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

            switch (compareOperation) {
                case CONTAINS:
                    filteredActualTable = new ContainsFilterTable(filteredActualTable, expectedTable, Arrays.asList(excludeCols));
                    break;
                default:
                    break;
            }

            DataSetAssertion.assertEqualsIgnoreCols(expectedTable, filteredActualTable, excludeCols);
        }
    }

    private List<Replacer> getReplacerInstances(Class<? extends Replacer>[] replacers) {
        List<Replacer> replacerInstances = new ArrayList<>();
        if (replacers != null && replacers.length > 0) {
            for (Class<? extends Replacer> replacerClass : replacers) {
                try {
                    replacerInstances.add(replacerClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalArgumentException(replacerClass.getName() + " could not be instantiated as Replacer");
                }
            }
        }
        return replacerInstances;
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
        if (!this.dbUnitConfig.equals(dbUnitConfig)) {
            this.dbUnitConfig = dbUnitConfig;
            riderDataSource = null;
            tableNameResolver = new TableNameResolver(dbUnitConfig);
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
