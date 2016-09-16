package com.github.database.rider.dataset;

import com.github.database.rider.api.dataset.DataSetExecutor;
import com.github.database.rider.api.dataset.YamlDataSet;
import com.github.database.rider.configuration.DBUnitConfig;
import com.github.database.rider.configuration.DataSetConfig;
import com.github.database.rider.exception.DataBaseSeedingException;
import com.github.database.rider.util.DriverUtils;
import com.github.database.rider.api.connection.ConnectionHolder;
import com.github.database.rider.api.dataset.JSONDataSet;
import com.github.database.rider.assertion.DataSetAssertion;
import com.github.database.rider.replacer.DateTimeReplacer;
import com.github.database.rider.replacer.ScriptReplacer;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.filter.SequenceTableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pestano on 26/07/15.
 */
public class DataSetExecutorImpl implements DataSetExecutor {

    public static final String DEFAULT_EXECUTOR_ID = "default";

    private static final Logger log = LoggerFactory.getLogger(DataSetExecutorImpl.class);

    private static Map<String, DataSetExecutorImpl> executors = new ConcurrentHashMap<>();

    private AtomicBoolean printDBUnitConfig = new AtomicBoolean(true);

    private DBUnitConfig dbUnitConfig;

    private static String SEQUENCE_TABLE_NAME;

    private DatabaseConnection databaseConnection;

    private ConnectionHolder connectionHolder;

    private String executorId;

    private List<String> tableNames;

    private String driverName;
    
    
    static {
        SEQUENCE_TABLE_NAME = System.getProperty("SEQUENCE_TABLE_NAME") == null ? "SEQ" : System.getProperty("SEQUENCE_TABLE_NAME");
    }


    public static DataSetExecutorImpl instance(ConnectionHolder connectionHolder) {

        if (connectionHolder == null) {
            throw new RuntimeException("Invalid connection");
        }
        //if no executor name is provided use default
        return instance(DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public static DataSetExecutorImpl instance(String executorId, ConnectionHolder connectionHolder) {

        if (connectionHolder == null) {
            throw new RuntimeException("Invalid connection");
        }
        DataSetExecutorImpl instance = executors.get(executorId);
        if (instance == null) {
            instance = new DataSetExecutorImpl(executorId, connectionHolder, DBUnitConfig.fromGlobalConfig().executorId(executorId));
            log.debug("creating executor instance " + executorId);
            executors.put(executorId, instance);
        } else if(!instance.dbUnitConfig.isCacheConnection()) {
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
        if(printDBUnitConfig.compareAndSet(true,false)){
            StringBuilder sb = new StringBuilder(150);
            sb.append("cacheConnection: ").append("" + dbUnitConfig.isCacheConnection()).append("\n").
                    append("cacheTableNames: ").append(dbUnitConfig.isCacheTableNames()).append("\n").
                    append("leakHunter: ").append("" + dbUnitConfig.isLeakHunter()).append("\n");

            for (Entry<String, Object> entry : dbUnitConfig.getProperties().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            log.info(String.format("DBUnit configuration for dataset executor '%s':\n"+sb.toString(),this.executorId));
        }

        if (dataSetConfig != null) {
            try {
                if(databaseConnection == null || !dbUnitConfig.isCacheConnection()){
                    initDatabaseConnection();
                }
                if (dataSetConfig.isDisableConstraints()) {
                    disableConstraints();
                }
                if (dataSetConfig.isCleanBefore()) {
                    try {
                        clearDatabase(dataSetConfig);
                    } catch (SQLException e) {
                        LoggerFactory.getLogger(DataSetExecutorImpl.class.getName()).warn("Could not clean database before test.", e);
                    }
                }

                if (dataSetConfig.getExecuteStatementsBefore() != null && dataSetConfig.getExecuteStatementsBefore().length > 0) {
                    executeStatements(dataSetConfig.getExecuteStatementsBefore());
                }

                if (dataSetConfig.getExecuteScriptsBefore() != null && dataSetConfig.getExecuteScriptsBefore().length > 0) {
                    for (int i = 0; i < dataSetConfig.getExecuteScriptsBefore().length; i++) {
                        executeScript(dataSetConfig.getExecuteScriptsBefore()[i]);
                    }
                }

                if (dataSetConfig.getName() != null && !"".equals(dataSetConfig.getName())) {
                    IDataSet resultingDataSet = loadDataSet(dataSetConfig.getName());

                    resultingDataSet = performSequenceFiltering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performTableOrdering(dataSetConfig, resultingDataSet);

                    resultingDataSet = performReplacements(resultingDataSet);

                    DatabaseOperation operation = dataSetConfig.getstrategy().getOperation();

                    operation.execute(databaseConnection, resultingDataSet);
                }

            } catch (Exception e) {
                throw new DataBaseSeedingException("Could not initialize dataset: " + dataSetConfig, e);
            }


        }
    }

    /**
     * @param name one or more (comma separated) dataset names to instance
     * @return loaded dataset (in case of multiple dataSets they will be merged in one using composite dataset)
     */
    public IDataSet loadDataSet(String name) throws DataSetException, IOException {
        String[] dataSetNames = name.trim().split(",");

        List<IDataSet> dataSets = new ArrayList<>();
        for (String dataSet : dataSetNames) {
            IDataSet target = null;
            String dataSetName = dataSet.trim();
            String extension = dataSetName.substring(dataSetName.lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "yml": {
                    target = new YamlDataSet(getDataSetStream(dataSetName));
                    break;
                }
                case "xml": {
                    target = new FlatXmlDataSetBuilder().build(getDataSetStream(dataSetName));
                    break;
                }
                case "csv": {
                    target = new CsvDataSet(new File(getClass().getClassLoader().getResource(dataSetName).getFile()).getParentFile());
                    break;
                }
                case "xls": {
                    target = new XlsDataSet(getDataSetStream(dataSetName));
                    break;
                }
                case "json": {
                    target = new JSONDataSet(getDataSetStream(dataSetName));
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

    @Override
    public ConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    private IDataSet performTableOrdering(DataSetConfig dataSet, IDataSet target) throws AmbiguousTableNameException {
        if (dataSet.getTableOrdering().length > 0) {
            target = new FilteredDataSet(new SequenceTableFilter(dataSet.getTableOrdering()), target);
        }
        return target;
    }

    private IDataSet performSequenceFiltering(DataSetConfig dataSet, IDataSet target) throws DataSetException, SQLException {
        if (dataSet.isUseSequenceFiltering()) {
            ITableFilter filteredTable = new DatabaseSequenceFilter(databaseConnection, target.getTableNames());
            target = new FilteredDataSet(filteredTable, target);
        }
        return target;
    }


    private void configDatabaseProperties() throws SQLException {
        DatabaseConfig config = databaseConnection.getConfig();
        for (Entry<String, Object> p : dbUnitConfig.getProperties().entrySet()) {
            config.setProperty(DatabaseConfig.findByShortName(p.getKey()).getProperty(),p.getValue());
        }

        //PROPERTY_DATATYPE_FACTORY
        String driverName = getDriverName(connectionHolder);
        if (DriverUtils.isHsql(driverName)) {
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
        } else if (DriverUtils.isH2(driverName)) {
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        } else if (DriverUtils.isMysql(driverName)) {
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        } else if (DriverUtils.isPostgre(driverName)) {
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else if (DriverUtils.isOracle(driverName)) {
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        }

    }

    private void disableConstraints() throws SQLException {

        String driverName = getDriverName(connectionHolder);
        if (DriverUtils.isHsql(driverName)) {
            connectionHolder.getConnection().createStatement().execute("SET DATABASE REFERENTIAL INTEGRITY FALSE;");
        }

        if (DriverUtils.isH2(driverName)) {
            connectionHolder.getConnection().createStatement().execute("SET foreign_key_checks = 0;");
        }

        if (DriverUtils.isMysql(driverName)) {
            connectionHolder.getConnection().createStatement().execute(" SET FOREIGN_KEY_CHECKS=0;");
        }

        if (DriverUtils.isPostgre(driverName) || DriverUtils.isOracle(driverName)) {
            connectionHolder.getConnection().createStatement().execute("SET CONSTRAINTS ALL DEFERRED;");
        }

    }

    private String getDriverName(ConnectionHolder connectionHolder) throws SQLException {

        if(driverName != null){
            return driverName;
        }
        return DriverUtils.getDriverName(connectionHolder.getConnection());
    }

    public void executeStatements(String[] statements) {
        if (statements != null && statements.length > 0 && !"".equals(statements[0].trim())) {
            try {
                boolean autoCommit = connectionHolder.getConnection().getAutoCommit();
                connectionHolder.getConnection().setAutoCommit(false);
                java.sql.Statement statement = connectionHolder.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                for (String stm : statements) {
                    statement.addBatch(stm);
                }
                statement.executeBatch();
                connectionHolder.getConnection().commit();
                connectionHolder.getConnection().setAutoCommit(autoCommit);
            } catch (Exception e) {
                log.error("Could not createDataSet statements:" + e.getMessage(), e);
            }

        }
    }

    private IDataSet performReplacements(IDataSet dataSet) {
        IDataSet replace = DateTimeReplacer.replace(dataSet);
        replace = ScriptReplacer.replace(replace);
        return replace;
    }


    private void initDatabaseConnection() throws DatabaseUnitException, SQLException {
         databaseConnection = new DatabaseConnection(connectionHolder.getConnection());
         configDatabaseProperties();
    }


    public void setConnectionHolder(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
        try {
            initDatabaseConnection();
        }catch (Exception e){
            log.error("Could not initialize dbunit connection.",e);
        }
    }

    public Connection getConnection() {
        try {
            return connectionHolder.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, DataSetExecutorImpl> getExecutors() {
        return executors;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof DataSetExecutorImpl == false) {
            return false;
        }
        DataSetExecutorImpl otherExecutor = (DataSetExecutorImpl) other;
        if (databaseConnection == null || otherExecutor.databaseConnection == null) {
            return false;
        }
        try {
            if (databaseConnection.getConnection() == null || otherExecutor.databaseConnection.getConnection() == null) {
                return false;
            }

            if (!databaseConnection.getConnection().getMetaData().getURL().equals(otherExecutor.databaseConnection.getConnection().getMetaData().getURL())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

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
        if (is == null) {//if not found try to get from datasets folder
            is = getClass().getResourceAsStream("/datasets" + dataSet);
        }
        if(is == null){
            throw new RuntimeException(String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",dataSet.substring(1)));
        }
        return is;
    }

    /**
     * @throws SQLException
     */
    public void clearDatabase(DataSetConfig dataset) throws SQLException {
        Connection connection = connectionHolder.getConnection();

        if (dataset != null && dataset.getTableOrdering() != null && dataset.getTableOrdering().length > 0) {
            for (String table : dataset.getTableOrdering()) {
                if (table.toUpperCase().contains(SEQUENCE_TABLE_NAME)) {
                    //tables containing 'SEQ'will NOT be cleared see https://github.com/rmpestano/dbunit-rules/issues/26
                    continue;
                }
                connection.createStatement().executeUpdate("DELETE FROM " + table + " where 1=1");
                connection.commit();
            }
        }
        //clear remaining tables in any order(if there are any, also no problem clearing again)
        List<String> tables = getTableNames(connection);
        for (String tableName : tables) {
            if (tableName.toUpperCase().contains("SEQ")) {
                //tables containing 'SEQ' will NOT be cleared see https://github.com/rmpestano/dbunit-rules/issues/26
                continue;
            }
            try {
                connection.createStatement().executeUpdate("DELETE FROM " + tableName + " where 1=1");
                connection.commit();
            } catch (Exception e) {
                log.warn("Could not clear table " + tableName + ", message:"+e.getMessage()+", cause: "+e.getCause());
            }
        }

    }

    private List<String> getTableNames(Connection con) {

        List<String> tables = new ArrayList<String>();
        
        if(tableNames != null && dbUnitConfig.isCacheTableNames()){
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
            
            if(tableNames == null){
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
            return result.getString("TABLE_SCHEMA");
        } catch (Exception e) {

        }
        return null;
    }

    public void executeScript(String scriptPath) {
        if (scriptPath != null && !"".equals(scriptPath)) {
            if (!scriptPath.startsWith("/")) {
                scriptPath = "/" + scriptPath;
            }
            URL resource = getClass().getResource(scriptPath.trim());
            String absolutePath = "";
            if (resource != null) {
                absolutePath = resource.getFile();
            } else {
                resource = getClass().getResource("/scripts" + scriptPath.trim());
                if (resource != null) {
                    absolutePath = resource.getFile();
                }
            }
            if (resource == null) {
                throw new RuntimeException(String.format("Could not find script %s in classpath", scriptPath));
            }

            File scriptFile = new File(absolutePath);

            String[] scriptsStatements = readScriptStatements(scriptFile);
            if (scriptsStatements != null && scriptsStatements.length > 0) {
                executeStatements(scriptsStatements);
            }
        }
    }

    private String[] readScriptStatements(File scriptFile) {
        RandomAccessFile rad = null;
        int lineNum = 0;
        try {
            rad = new RandomAccessFile(scriptFile, "r");
            String line;
            List<String> scripts = new ArrayList<>();
            while ((line = rad.readLine()) != null) {
                //a line can have multiple scripts separated by ;
                String[] lineScripts = line.split(";");
                for (int i = 0; i < lineScripts.length; i++) {
                    scripts.add(lineScripts[i]);
                }
                lineNum++;
            }
            return scripts.toArray(new String[scripts.size()]);
        } catch (Exception e) {
            log.warn(String.format("Could not read script file %s. Error in line %d.", scriptFile.getAbsolutePath(), lineNum), e);
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


    public void compareCurrentDataSetWith(DataSetConfig expectedDataSetConfig, String[] excludeCols) throws DatabaseUnitException {
        IDataSet current = null;
        IDataSet expected = null;
        try {
            if (databaseConnection == null) {
                //no dataset was created yet (e.g only @ExpectedDataSet declared in test method)
                initDatabaseConnection();
            }
            current = databaseConnection.createDataSet();
            expected = loadDataSet(expectedDataSetConfig.getName());
        } catch (Exception e) {
            throw new RuntimeException("Could not create dataset to compare.", e);
        }
        String[] tableNames = null;
        try {
            tableNames = expected.getTableNames();
        } catch (DataSetException e) {
            throw new RuntimeException("Could extract dataset table names.", e);
        }

        for (String tableName : expected.getTableNames()) {
            ITable expectedTable = null;
            ITable actualTable = null;
            try {
                expectedTable = expected.getTable(tableName);
                actualTable = current.getTable(tableName);
            } catch (DataSetException e) {
                throw new RuntimeException("DataSet comparison failed due to following exception: ", e);
            }
            ITable filteredActualTable = DefaultColumnFilter.includedColumnsTable(actualTable, expectedTable.getTableMetaData().getColumns());
            DataSetAssertion.assertEqualsIgnoreCols(expectedTable, filteredActualTable, excludeCols);
        }

    }

    public void setDBUnitConfig(DBUnitConfig dbUnitConfig) {
        this.dbUnitConfig = dbUnitConfig;
    }

    @Override
    public DBUnitConfig getDBUnitConfig() {
        return dbUnitConfig;
    }


    public DatabaseConnection getDBUnitConnection() {
        return databaseConnection;
    }
}
