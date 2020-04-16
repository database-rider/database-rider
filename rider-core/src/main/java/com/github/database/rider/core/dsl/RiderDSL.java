package com.github.database.rider.core.dsl;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;

import java.sql.Connection;

public class RiderDSL {

    private static RiderDSL INSTANCE;
    private Connection connection;
    private DataSetConfig dataSetConfig;
    private DBUnitConfig dbUnitConfig;


    public void createDataSet() {
        validateConnection(connection);
        validateDataSetConfig(dataSetConfig);
        final DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(dataSetConfig.getExecutorId(), new ConnectionHolderImpl(connection));
        if (dbUnitConfig != null) {
            dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        }
        dataSetExecutor.createDataSet(dataSetConfig);
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RiderDSL();
        }
    }

    public static DataSetConfigDSL withConnection(Connection connection) {
        createInstance();
        validateConnection(connection);
        INSTANCE.connection = connection;
        return new DataSetConfigDSL();
    }

    /**
     * Reuses current connection configured in the DSL
     * @return DataSet config DSL
     */
    public static DataSetConfigDSL withConnection() {
        return withConnection(INSTANCE.connection);
    }

    public static class DataSetConfigDSL {

        public static DBUnitConfigDSL withDataSet(DataSetConfig dataSetConfig) {
            validateDataSetConfig(dataSetConfig);
            INSTANCE.dataSetConfig = dataSetConfig;
            return new DBUnitConfigDSL();
        }

        public static DBUnitConfigDSL withDataSet() {
            return withDataSet(INSTANCE.dataSetConfig);
        }

    }

    public static class DBUnitConfigDSL {

        public static RiderDSL withDBUnitConfig(DBUnitConfig dbUnitConfig) {
            INSTANCE.dbUnitConfig = dbUnitConfig;
            return INSTANCE;
        }

        public static void createDataSet() {
            INSTANCE.createDataSet();
        }
    }

    private static void validateConnection(Connection connection) {
        if (connection == null) {
            throw new RuntimeException("Invalid jdbc connection.");
        }
    }

    private static void validateDataSetConfig(DataSetConfig dataSetConfig) {
        if (dataSetConfig == null || (!dataSetConfig.hasDataSets() && !dataSetConfig.hasDataSetProvider())) {
            throw new RuntimeException("Invalid dataset configuration. You must provide at least one dataset or dataset provider.");
        }
    }

}
