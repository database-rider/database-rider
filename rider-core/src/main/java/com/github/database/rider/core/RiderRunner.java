package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.exporter.DataSetExporter;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;


public class RiderRunner {

    private static final Logger logger = LoggerFactory.getLogger(DbUnitAssert.class);

    public void setup(RiderTestContext riderTestContext) throws SQLException {
        DBUnitConfig dbUnitConfig = resolveDBUnitConfig(riderTestContext);
        DataSetExecutor executor = riderTestContext.getDataSetExecutor();
        executor.setDBUnitConfig(dbUnitConfig);
        if (executor.getRiderDataSource().getDBUnitConnection().getConnection() == null) {
            ConnectionConfig connectionConfig = executor.getDBUnitConfig().getConnectionConfig();
            executor.initConnectionFromConfig(connectionConfig);
        }
    }

    public void runBeforeTest(RiderTestContext riderTestContext) throws SQLException {
        DataSetExecutor executor = riderTestContext.getDataSetExecutor();

        DataSet dataSet = riderTestContext.getAnnotation(DataSet.class);

        if (dataSet != null) {
            DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);
            try {
                executor.createDataSet(dataSetConfig);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Could not create dataset for test '%s'.", riderTestContext.getMethodName()), e);
            }
            if (dataSetConfig.isTransactional()) {
                riderTestContext.beginTransaction();
            }
        }
    }

    public void runAfterTest(RiderTestContext riderTestContext) throws SQLException, DatabaseUnitException {
        DataSet dataSet = riderTestContext.getAnnotation(DataSet.class);
        if (dataSet != null) {
            DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);

            if (dataSetConfig.isTransactional()) {
                riderTestContext.commit();
            }
        }
        performDataSetComparison(riderTestContext);
    }

    public void teardown(RiderTestContext riderTestContext) throws SQLException {
        exportDataSet(riderTestContext);
        String currentMethod = riderTestContext.getMethodName();
        DataSetExecutor executor = riderTestContext.getDataSetExecutor();
        DataSet dataSet = riderTestContext.getAnnotation(DataSet.class);
        if (dataSet != null) {
            DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);
            if (dataSetConfig.isTransactional()) {
                riderTestContext.rollback();
            }
            if (dataSetConfig.getExecuteStatementsAfter() != null && dataSetConfig.getExecuteStatementsAfter().length > 0) {
                try {
                    executor.executeStatements(dataSetConfig.getExecuteStatementsAfter());
                } catch (Exception e) {
                    logger.error(currentMethod + "() - Could not execute statements after:" + e.getMessage(), e);
                }
            }
            if (dataSetConfig.getExecuteScriptsAfter() != null && dataSetConfig.getExecuteScriptsAfter().length > 0) {
                try {
                    for (String scriptPath : dataSetConfig.getExecuteScriptsAfter()) {
                        executor.executeScript(scriptPath);
                    }
                } catch (Exception e) {
                    logger.error(currentMethod + "() - Could not execute scriptsAfter:" + e.getMessage(), e);
                }
            }

            if (dataSetConfig.isCleanAfter()) {
                executor.clearDatabase(dataSetConfig);
            }

            try {
                executor.enableConstraints();
            } catch (SQLException e) {
                logger.warn("Could not enable constraints.", e);
            }

            riderTestContext.clearEntityManager();
        }

        if (!executor.getDBUnitConfig().isCacheConnection() && !executor.getRiderDataSource().getDBUnitConnection().getConnection().isClosed()) {
            executor.getRiderDataSource().getDBUnitConnection().getConnection().close();
            ((DataSetExecutorImpl) executor).clearRiderDataSource();
        }
    }

    private void exportDataSet(RiderTestContext riderTestContext) {
        ExportDataSet exportDataSet = riderTestContext.getAnnotation(ExportDataSet.class);
        if (exportDataSet != null) {
            DataSetExportConfig exportConfig = DataSetExportConfig.from(exportDataSet);
            String outputName = exportConfig.getOutputFileName();
            if (outputName == null || "".equals(outputName.trim())) {
                outputName = riderTestContext.getMethodName().toLowerCase() + "." + exportConfig.getDataSetFormat().name().toLowerCase();
            }
            exportConfig.outputFileName(outputName);
            try {
                DataSetExporter.getInstance().export(riderTestContext.getDataSetExecutor().getRiderDataSource().getDBUnitConnection(), exportConfig);
            } catch (Exception e) {
                logger.error("Could not export dataset after method " + riderTestContext.getMethodName(), e);
            }
        }
    }

    private DBUnitConfig resolveDBUnitConfig(RiderTestContext riderTestContext) {
        DBUnit dbUnitConfig = riderTestContext.getAnnotation(DBUnit.class);

        if (dbUnitConfig != null) {
            return DBUnitConfig.from(dbUnitConfig);
        } else {
            return DBUnitConfig.fromGlobalConfig();
        }
    }

    private void performDataSetComparison(RiderTestContext riderTestContext) throws DatabaseUnitException {
        ExpectedDataSet expectedDataSet = riderTestContext.getAnnotation(ExpectedDataSet.class);

        if (expectedDataSet != null) {
            riderTestContext.getDataSetExecutor()
                    .compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value())
                                    .disableConstraints(true).datasetProvider(expectedDataSet.provider()),
                            expectedDataSet.ignoreCols(),
                            expectedDataSet.replacers(),
                            expectedDataSet.orderBy(),
                            expectedDataSet.compareOperation());
        }
    }
}
