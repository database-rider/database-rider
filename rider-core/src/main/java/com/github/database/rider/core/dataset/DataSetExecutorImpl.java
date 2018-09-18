package com.github.database.rider.core.dataset;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.dataset.JSONDataSet;
import com.github.database.rider.core.api.dataset.ScriptableDataSet;
import com.github.database.rider.core.api.dataset.YamlDataSet;
import com.github.database.rider.core.assertion.DataSetAssertion;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import com.github.database.rider.core.replacers.Replacer;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.filter.SequenceTableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private final AtomicBoolean printDBUnitConfig = new AtomicBoolean(true);

    private DBUnitConfig dbUnitConfig;

    private RiderDataSource riderDataSource;

    private ConnectionHolder connectionHolder;

    private String executorId;

    private List<String> tableNames;

    private String schemaName;

    private boolean isContraintsDisabled = false;

    static {
        SEQUENCE_TABLE_NAME = System.getProperty("SEQUENCE_TABLE_NAME") == null ? "SEQ"
                : System.getProperty("SEQUENCE_TABLE_NAME");
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

                if (dataSetConfig.hasDatasets()) {
                    IDataSet resultingDataSet = loadDataSets(dataSetConfig.getDatasets());

                    resultingDataSet = performSequenceFiltering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performTableOrdering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performReplacements(resultingDataSet);

                    DatabaseOperation operation = dataSetConfig.getstrategy().getOperation();

                    operation.execute(getRiderDataSource().getDBUnitConnection(), resultingDataSet);
                }

            } catch (Exception e) {
                throw new DataBaseSeedingException("Could not initialize dataset: " + dataSetConfig, e);
            }

        }
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
        switch (getRiderDataSource().getDBType()) {
            case HSQLDB:
                getRiderDataSource().getConnection().createStatement().execute("SET DATABASE REFERENTIAL INTEGRITY FALSE;");
                break;
            case H2:
                getRiderDataSource().getConnection().createStatement().execute("SET foreign_key_checks = 0;");
                break;
            case MYSQL:
                getRiderDataSource().getConnection().createStatement().execute(" SET FOREIGN_KEY_CHECKS=0;");
                break;
            case POSTGRESQL:
                /*
             * preferable way because constraints are automatically re-enabled afer transaction. The only downside is
             * that constrains need to be marked as deferrable: ALTER TABLE table_name ADD CONSTRAINT constraint_uk
             * UNIQUE(column_1, column_2) DEFERRABLE INITIALLY IMMEDIATE;
                 */
                getRiderDataSource().getConnection().createStatement().execute("SET CONSTRAINTS ALL DEFERRED;");
                break;
            case ORACLE:
                // adapted from Unitils:
                // https://github.com/arteam/unitils/blob/master/unitils-core/src/main/java/org/unitils/core/dbsupport/OracleDbSupport.java#L190
                Connection connection = null;
                Statement queryStatement = null;
                ResultSet resultSet = null;
                String schemaName = "";
                String tableName = "";
                try {
                    connection = getRiderDataSource().getConnection();
                    queryStatement = connection.createStatement();
                    schemaName = resolveSchema();// default schema
                    // to be sure no recycled items are handled, all items with a name that starts with BIN$ will be
                    // filtered out.
                    resultSet = queryStatement.executeQuery(
                            "select TABLE_NAME, CONSTRAINT_NAME from ALL_CONSTRAINTS where CONSTRAINT_TYPE = 'R' "
                            + (schemaName != null ? "and OWNER = '" + schemaName + "'" : "")
                            + " and CONSTRAINT_NAME not like 'BIN$%' and STATUS <> 'DISABLED'");
                    while (resultSet.next()) {
                        schemaName = resolveSchema(resultSet);// result set schema
                        tableName = resultSet.getString("TABLE_NAME");
                        boolean hasSchema = schemaName != null && !"".equals(schemaName.trim());
                        String constraintName = resultSet.getString("CONSTRAINT_NAME");
                        String qualifiedTableName = hasSchema ? "'" + schemaName + "'.'" + tableName + "'"
                                : "'" + tableName + "'";
                        executeStatements(
                                "alter table " + qualifiedTableName + " disable constraint '" + constraintName + "'");
                    }
                    break;
                } catch (Exception e) {
                    throw new RuntimeException("Error while disabling referential constraints on schema " + schemaName, e);
                }
        }

        isContraintsDisabled = true;

    }

    @Override
    public void enableConstraints() throws SQLException {
        if (isContraintsDisabled) {
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
                    case ORACLE:
                        // adapted from Unitils:
                        // https://github.com/arteam/unitils/blob/master/unitils-core/src/main/java/org/unitils/core/dbsupport/OracleDbSupport.java#L190
                        ResultSet resultSet = null;
                        String tableName = "";
                        try {
                            String schemaName = resolveSchema();
                            // to be sure no recycled items are handled, all items with a name that starts with BIN$ will be
                            // filtered out.
                            resultSet = statement.executeQuery(
                                    "select TABLE_NAME, CONSTRAINT_NAME from ALL_CONSTRAINTS where CONSTRAINT_TYPE = 'R' "
                                    + (schemaName != null ? "and OWNER = '" + schemaName + "'" : "")
                                    + " and CONSTRAINT_NAME not like 'BIN$%' and STATUS = 'DISABLED'");
                            while (resultSet.next()) {
                                tableName = resultSet.getString("TABLE_NAME");
                                boolean hasSchema = schemaName != null && !"".equals(schemaName.trim());
                                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                                String qualifiedTableName = hasSchema ? "'" + schemaName + "'.'" + tableName + "'"
                                        : "'" + tableName + "'";
                                executeStatements(
                                        "alter table " + qualifiedTableName + " enable constraint '" + constraintName + "'");
                            }
                            break;
                        } catch (Exception e) {
                            throw new RuntimeException("Error while enabling referential constraints on schema " + schemaName,
                                    e);
                        } finally {
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        }
                }

                isContraintsDisabled = false;
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
                if(statement != null) {
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

        ReplacementDataSet replacementSet = new ReplacementDataSet(dataSet);

        // convert to set to remove duplicates
        Set<Replacer> replacers = new HashSet<>((List<Replacer>) dbUnitConfig.getProperties().get("replacers"));
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

            Connection connection;

            @Override
            public Connection getConnection() throws SQLException {
                if (connection == null) {
                    connection = getConnectionFromConfig(connectionConfig);
                }

                return connection;
            }
        });
    }

    private Connection getConnectionFromConfig(ConnectionConfig connectionConfig) throws SQLException {
        if ("".equals(connectionConfig.getUrl()) || "".equals(connectionConfig.getUser())) {
            throw new RuntimeException("Could not create JDBC connection, provide a connection at test level or via configuration, see documentation here: https://github.com/database-rider/database-rider#jdbc-connection");
        }

        if (!"".equals(connectionConfig.getDriver())) {
            try {
                Class.forName(connectionConfig.getDriver());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

        return DriverManager.getConnection(connectionConfig.getUrl(), connectionConfig.getUser(),
                connectionConfig.getPassword());
    }

    public Connection getConnection() {
        try {
            return getRiderDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DataSetExecutorImpl == false) {
            return false;
        }
        DataSetExecutorImpl otherExecutor = (DataSetExecutorImpl) other;
        if (riderDataSource == null || otherExecutor.riderDataSource == null) {
            return false;
        }
        try {
            if (riderDataSource.getDBUnitConnection().getConnection() == null
                    || otherExecutor.riderDataSource.getDBUnitConnection().getConnection() == null) {
                return false;
            }

            if (!riderDataSource.getDBUnitConnection().getConnection().getMetaData().getURL().equals(
                    otherExecutor.riderDataSource.getDBUnitConnection().getConnection().getMetaData().getURL())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
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
    public void clearDatabase(DataSetConfig dataset) throws SQLException {
        Connection connection = getRiderDataSource().getConnection();

        if (dataset != null && dataset.getTableOrdering() != null && dataset.getTableOrdering().length > 0) {
            for (String table : dataset.getTableOrdering()) {
                if (table.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                    // tables containing 'SEQ'will NOT be cleared see
                    // https://github.com/rmpestano/dbunit-rules/issues/26
                    continue;
                }
                connection.createStatement().executeUpdate("DELETE FROM " + table + " where 1=1");
                connection.commit();
            }
        }
        // clear remaining tables in any order(if there are any, also no problem clearing again)
        List<String> tables = getTableNames(connection);
        for (String tableName : tables) {
            if (tableName.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                // tables containing 'SEQ' will NOT be cleared see https://github.com/rmpestano/dbunit-rules/issues/26
                continue;
            }
            try {
                connection.createStatement().executeUpdate("DELETE FROM " + tableName + " where 1=1");
                connection.commit();
            } catch (Exception e) {
                log.warn("Could not clear table " + tableName + ", message:" + e.getMessage() + ", cause: "
                        + e.getCause());
            }
        }

    }

    private List<String> getTableNames(Connection con) {

        List<String> tables = new ArrayList<String>();

        if (tableNames != null && dbUnitConfig.isCacheTableNames()) {
            return tableNames;
        }

        ResultSet result = null;
        try {
            DatabaseMetaData metaData = con.getMetaData();

            result = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (result.next()) {
                String schema = resolveSchema(result);
                String name = result.getString("TABLE_NAME");
                tables.add(schema != null ? schema + "." + name : name);
            }

            if (tableNames == null) {
                this.tableNames = new ArrayList<>();
                this.tableNames.addAll(tables);
            }

            return tables;
        } catch (SQLException ex) {
            log.warn("An exception occured while trying to"
                    + "analyse the database.", ex);
            return new ArrayList<String>();
        }
    }

    private String resolveSchema(ResultSet result) {
        try {
            if (schemaName == null) {
                schemaName = result.getString("TABLE_SCHEMA");
            }
            return schemaName;
        } catch (Exception e) {

        }
        return null;
    }

    private String resolveSchema() {
        try {
            if (schemaName == null) {
                DatabaseMetaData metaData = getRiderDataSource().getConnection().getMetaData();

                ResultSet result = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                schemaName = resolveSchema(result);
            }
            return schemaName;
        } catch (Exception e) {

        }
        return null;
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
        String[] result = null;
        String absolutePath = resource.getFile();
        if (resource.getProtocol().equals("jar")) {
            result = readScriptStatementsFromJar(absolutePath);
        } else {
            result = readScriptStatementsFromFile(new File(absolutePath));
        }
        return result;
    }

    private String[] readScriptStatementsFromJar(String absolutePath) {
        String jarEntry = "jar:" + absolutePath;
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

    private String[] readScriptStatementsFromFile(File scriptFile) {
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
                    scripts.add(lineScripts[i]);
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

    @Override
    public void compareCurrentDataSetWith(DataSetConfig expectedDataSetConfig, String[] excludeCols)
            throws DatabaseUnitException {
        IDataSet current = null;
        IDataSet expected = null;
        try {
            current = getRiderDataSource().getDBUnitConnection().createDataSet();
            expected = loadDataSets(expectedDataSetConfig.getDatasets());
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
            ITable filteredActualTable = DefaultColumnFilter.includedColumnsTable(actualTable,
                    expectedTable.getTableMetaData().getColumns());
            DataSetAssertion.assertEqualsIgnoreCols(expectedTable, filteredActualTable, excludeCols);
        }

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
    public RiderDataSource getRiderDataSource() throws SQLException {
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
