package com.github.database.rider.core.dsl;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.configuration.ExpectedDataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import org.dbunit.DatabaseUnitException;

import java.sql.Connection;

/**
 * @author rmpestano
 * @since 1.13.0
 * DSL for populating database which is an alternative to `@DataSet` annotation
 */
public class RiderDSL {

    private static ThreadLocal<RiderDSL> RIDER_DSL_THREAD_LOCAL = new ThreadLocal<>();
    private Connection connection;
    private DataSetConfig dataSetConfig;
    private DBUnitConfig dbUnitConfig;

    /**
     * Creates a dataset in database using the provided connection ({@link Connection}), dataset configuration ({@link DataSetConfig}
     * and dbunit configuration ({@link DBUnitConfig})
     */
    public void createDataSet() {
        validateConnection();
        final DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(dataSetConfig.getExecutorId(), new ConnectionHolderImpl(connection));
        if (dbUnitConfig != null) {
            dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        }
        dataSetExecutor.createDataSet(dataSetConfig);
    }

    public void expectDataSet() throws DatabaseUnitException {
        expectDataSet(new ExpectedDataSetConfig());
    }

    /**
     * Compares current database state with an expected dataset. The same way as in @ExpectedDataSet.
     * @param expectedDataSetConfig expected dataset configuration
     * @throws DatabaseUnitException exception when comparison fails
     * @since 1.15.0
     */
    public void expectDataSet(ExpectedDataSetConfig expectedDataSetConfig) throws DatabaseUnitException {
        validateConnection();
        final DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(dataSetConfig.getExecutorId(), new ConnectionHolderImpl(connection));
        if (dbUnitConfig != null) {
            dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        }
        dataSetExecutor.compareCurrentDataSetWith(dataSetConfig, expectedDataSetConfig.getIgnoreCols(), expectedDataSetConfig.getReplacers(),
                expectedDataSetConfig.getOrderBy(), expectedDataSetConfig.getCompareOperation());
    }

    /**
     * Configures the DSL with provided JDBC connection
     *
     * @param connection jdbc connection to be used when populating the database
     * @return {@link DataSetConfigDSL} instance
     */
    public static DataSetConfigDSL withConnection(Connection connection) {
        getInstance().connection = connection;
        return new DataSetConfigDSL();
    }


    private static RiderDSL getInstance() {
        if (RIDER_DSL_THREAD_LOCAL.get() == null) {
            RIDER_DSL_THREAD_LOCAL.set(new RiderDSL());
        }
        return RIDER_DSL_THREAD_LOCAL.get();
    }

    /**
     * Reuses current connection configured in the DSL
     *
     * @return {@link DataSetConfigDSL} instance
     */
    public static DataSetConfigDSL withConnection() {
        return withConnection(getInstance().connection);
    }

    public static class DataSetConfigDSL {

        /**
         * Configures the DSL with provided DataSet configuration
         *
         * @param dataSetConfig {@link DataSetConfig}
         * @return A {@link DBUnitConfigDSL} to create DBUnit configuration ({@link DBUnitConfig}
         */
        public static DBUnitConfigDSL withDataSetConfig(DataSetConfig dataSetConfig) {
            getInstance().dataSetConfig = dataSetConfig;
            return new DBUnitConfigDSL();
        }

        /**
         * Reuses dataset configuration already provided to the DSL
         *
         * @return A {@link DBUnitConfigDSL} instance to create DBUnit configuration ({@link DBUnitConfig}
         */
        public static DBUnitConfigDSL withDataSetConfig() {
            if(getInstance().dataSetConfig == null) {
                getInstance().dataSetConfig = new DataSetConfig();
            }
            return withDataSetConfig(getInstance().dataSetConfig);
        }

        /**
         * A shortcut for <code>RiderDSL.withConnection(emProvider.connection())
         *                 .withDataSetConfig(new DataSetConfig()
         *                         .cleanBefore(true))
         *                 .createDataSet();</code>
         * @since 1.15.0
         *
         */
        public static void cleanDB() {
            validateConnection();
            getInstance().dataSetConfig = new DataSetConfig().cleanBefore(true);
            getInstance().createDataSet();
        }

    }

    public static class DBUnitConfigDSL {

        /**
         * Configures the DSL with provided DBUnit configuration
         *
         * @param dbUnitConfig {@link DBUnitConfig}
         * @return {@link RiderDSL} instance
         */
        public static RiderDSL withDBUnitConfig(DBUnitConfig dbUnitConfig) {
            RiderDSL riderDSL = getInstance();
            riderDSL.dbUnitConfig = dbUnitConfig;
            return riderDSL;
        }

        /**
         * Creates a dataset in database using the provided connection ({@link Connection}), dataset configuration ({@link DataSetConfig}
         * and dbunit configuration ({@link DBUnitConfig})
         */
        public static void createDataSet() {
            getInstance().createDataSet();
        }

        public static void expectDataSet() throws DatabaseUnitException {
            getInstance().expectDataSet();
        }

        /**
         * Compares current database state with expected dataset
         * @param expectedDataSetConfig expected dataset configuration
         * @throws DatabaseUnitException exception when comparison fails
         */
        public static void expectDataSet(ExpectedDataSetConfig expectedDataSetConfig) throws DatabaseUnitException {
            getInstance().expectDataSet(expectedDataSetConfig);
        }
    }

    private static void validateConnection() {
        if (getInstance().connection == null) {
            throw new RuntimeException("Invalid jdbc connection.");
        }
    }

}
