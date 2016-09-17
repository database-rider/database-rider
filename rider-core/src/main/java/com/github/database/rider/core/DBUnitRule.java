package com.github.database.rider.core;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.expoter.DataSetExportConfig;
import com.github.database.rider.core.api.expoter.ExportDataSet;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.exporter.DataSetExporter;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.leak.LeakHunterException;
import com.github.database.rider.core.leak.LeakHunterFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
public class DBUnitRule implements TestRule {

	
    private static final Logger logger = LoggerFactory.getLogger(DbUnitAssert.class);


    private String currentMethod;

    private DataSetExecutor executor;

    private DBUnitRule() {
    }

    public final static DBUnitRule instance() {
        return instance(new ConnectionHolderImpl(null));
    }

    public final static DBUnitRule instance(Connection connection) {
        return instance(new ConnectionHolderImpl(connection));
    }

    public final static DBUnitRule instance(String executorName, Connection connection) {
        return instance(executorName, new ConnectionHolderImpl(connection));
    }

    public final static DBUnitRule instance(ConnectionHolder connectionHolder) {
        return instance(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public final static DBUnitRule instance(String executorName, ConnectionHolder connectionHolder) {
        DBUnitRule instance = new DBUnitRule();
        instance.init(executorName, connectionHolder);
        return instance;
    }


    @Override
    public Statement apply(final Statement statement, final Description description) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                DBUnitConfig dbUnitConfig = resolveDBUnitConfig(description);
                currentMethod = description.getMethodName();
                DataSet dataSet = resolveDataSet(description);
                if (dataSet != null) {
                    final DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);
                    final String datasetExecutorId = dataSetConfig.getExecutorId();
                    LeakHunter leakHunter = null;
                    boolean executorNameIsProvided = datasetExecutorId != null && !"".equals(datasetExecutorId.trim());
                    if (executorNameIsProvided) {
                        executor = DataSetExecutorImpl.getExecutorById(datasetExecutorId);
                    }
                    try {
                        if (executor.getConnectionHolder() == null || executor.getConnectionHolder().getConnection() == null) {
                            executor.setConnectionHolder(new ConnectionHolderImpl(getConnectionFrom(dbUnitConfig)));
                        }
                        executor.setDBUnitConfig(dbUnitConfig);
                        executor.createDataSet(dataSetConfig);
                    } catch (final Exception e) {
                        throw new RuntimeException(String.format("Could not create dataset for test '%s'.", description.getMethodName()), e);
                    }
                    boolean isTransactional = false;
                    try {
                        isTransactional = dataSetConfig.isTransactional();
                        if (isTransactional) {
                            if(isEntityManagerActive()){
                                em().getTransaction().begin();
                            }else{
                                Connection connection = executor.getConnectionHolder().getConnection();
                                connection.setAutoCommit(false);
                            }
                        }
                        boolean leakHunterActivated = dbUnitConfig.isLeakHunter();
                        int openConnectionsBefore = 0;
                        if (leakHunterActivated) {
                            leakHunter = LeakHunterFactory.from(executor.getConnectionHolder().getConnection());
                            openConnectionsBefore = leakHunter.openConnections();
                        }
                        statement.evaluate();

                        int openConnectionsAfter = 0;
                        if (leakHunterActivated) {
                            openConnectionsAfter = leakHunter.openConnections();
                            if (openConnectionsAfter > openConnectionsBefore) {
                                throw new LeakHunterException(currentMethod, openConnectionsAfter - openConnectionsBefore);
                            }
                        }

                        if (isTransactional) {
                            if(isEntityManagerActive() && em().getTransaction().isActive()){
                                em().getTransaction().commit();
                            } else{
                                Connection connection = executor.getConnectionHolder().getConnection();
                                connection.commit();
                                connection.setAutoCommit(false);
                            }
                        }
                        performDataSetComparison(description);
                    } catch (Exception e) {
                        if (isTransactional){
                            if(isEntityManagerActive() && em().getTransaction().isActive()) {
                                em().getTransaction().rollback();
                            } else {
                                Connection connection = executor.getConnectionHolder().getConnection();
                                connection.rollback();
                            }
                        }
                        throw e;
                    } finally {
                        exportDataSet(executor, description);
                        if (dataSetConfig != null && dataSetConfig.getExecuteStatementsAfter() != null && dataSetConfig.getExecuteStatementsAfter().length > 0) {
                            try {
                                executor.executeStatements(dataSetConfig.getExecuteStatementsAfter());
                            } catch (Exception e) {
                                logger.error(currentMethod + "() - Could not execute statements after:" + e.getMessage(), e);
                            }
                        }//end execute statements
                        if (dataSetConfig != null && dataSetConfig.getExecuteScriptsAfter() != null && dataSetConfig.getExecuteScriptsAfter().length > 0) {
                            try {
                                for (int i = 0; i < dataSetConfig.getExecuteScriptsAfter().length; i++) {
                                    executor.executeScript(dataSetConfig.getExecuteScriptsAfter()[i]);
                                }
                            } catch (Exception e) {
                                if (e instanceof DatabaseUnitException) {
                                    throw e;
                                }
                                logger.error(currentMethod + "() - Could not execute scriptsAfter:" + e.getMessage(), e);
                            }
                        }//end execute scripts

                        if (dataSetConfig.isCleanAfter()) {
                            executor.clearDatabase(dataSetConfig);
                        }
                    }
                    //no dataset provided, only export and evaluate expected dataset
                } else {
                    if(executor.getConnectionHolder() == null || executor.getConnectionHolder().getConnection() == null){
                        executor.setConnectionHolder(new ConnectionHolderImpl(getConnectionFrom(dbUnitConfig)));
                    }
                    exportDataSet(executor, description);
                    statement.evaluate();
                    performDataSetComparison(description);
                }

            }

            private Connection getConnectionFrom(DBUnitConfig dbUnitConfig) {
                ConnectionConfig connectionConfig = dbUnitConfig.getConnectionConfig();
                if ("".equals(connectionConfig.getUrl()) || "".equals(connectionConfig.getUser())) {
                    throw new RuntimeException(String.format("Could not create JDBC connection for method %s, provide a connection at test level or via configuration, see documentation here: https://github.com/rmpestano/dbunit-rules#jdbc-connection", currentMethod));
                }
                try {
                    if (!"".equals(connectionConfig.getDriver())) {
                        Class.forName(connectionConfig.getDriver());
                    }
                    return DriverManager.getConnection(connectionConfig.getUrl(), connectionConfig.getUser(), connectionConfig.getPassword());
                } catch (Exception e) {
                    logger.error("Could not create JDBC connection for method " + currentMethod, e);
                }
                return null;
            }


        };
    }

    private void exportDataSet(DataSetExecutor executor, Description description) {
        ExportDataSet exportDataSet = resolveExportDataSet(description);
        if (exportDataSet != null) {
            DataSetExportConfig exportConfig = DataSetExportConfig.from(exportDataSet);
            String outputName = exportConfig.getOutputFileName();
            if (outputName == null || "".equals(outputName.trim())) {
                outputName = description.getMethodName().toLowerCase() + "." + exportConfig.getDataSetFormat().name().toLowerCase();
            }
            exportConfig.outputFileName(outputName);
            try {
                DataSetExporter.getInstance().export(executor.getDBUnitConnection(), exportConfig);
            } catch (Exception e) {
            	logger.error("Could not export dataset after method " + description.getMethodName(), e);
            }
        }
    }

    private ExportDataSet resolveExportDataSet(Description description) {
        ExportDataSet exportDataSet = description.getAnnotation(ExportDataSet.class);
        if (exportDataSet == null) {
            exportDataSet = description.getTestClass().getAnnotation(ExportDataSet.class);
        }
        return exportDataSet;
    }

    private DataSet resolveDataSet(Description description) {
        DataSet dataSet = description.getAnnotation(DataSet.class);
        if (dataSet == null) {
            dataSet = description.getTestClass().getAnnotation(DataSet.class);
        }
        return dataSet;
    }

    private DBUnitConfig resolveDBUnitConfig(Description description) {
        DBUnit dbUnitConfig = description.getAnnotation(DBUnit.class);
        if (dbUnitConfig == null) {
            dbUnitConfig = description.getTestClass().getAnnotation(DBUnit.class);
        }

        if (dbUnitConfig != null) {
            return DBUnitConfig.from(dbUnitConfig);
        } else {
            return DBUnitConfig.fromGlobalConfig();
        }
    }

    private void performDataSetComparison(Description description) throws DatabaseUnitException {
        ExpectedDataSet expectedDataSet = description.getAnnotation(ExpectedDataSet.class);
        if (expectedDataSet == null) {
            //try to infer from class level annotation
            expectedDataSet = description.getTestClass().getAnnotation(ExpectedDataSet.class);
        }
        if (expectedDataSet != null) {
            executor.compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value()).disableConstraints(true), expectedDataSet.ignoreCols());
        }
    }

    private void init(String name, ConnectionHolder connectionHolder) {
        DataSetExecutorImpl instance = DataSetExecutorImpl.getExecutorById(name);
        if (instance == null) {
            instance = DataSetExecutorImpl.instance(name, connectionHolder);
            DataSetExecutorImpl.getExecutors().put(name, instance);
        } else {
            instance.setConnectionHolder(connectionHolder);
        }
        executor = instance;

    }

    public DataSetExecutor getDataSetExecutor() {
        return executor;
    }


}