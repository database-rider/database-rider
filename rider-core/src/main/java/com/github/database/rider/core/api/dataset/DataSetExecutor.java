package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.replacers.Replacer;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by pestano on 01/08/15.
 */
public interface DataSetExecutor{

    /**
     * creates a dataset into executor's database connection using given dataSetConfig
     * @param dataSetConfig dataset configuration
     */
    void createDataSet(DataSetConfig dataSetConfig);

    IDataSet loadDataSet(String name) throws DataSetException, IOException;

    IDataSet loadDataSets(String[] datasets) throws DataSetException, IOException;

    void initConnectionFromConfig(ConnectionConfig connectionConfig);

    void clearDatabase(DataSetConfig dataset) throws SQLException;

    void executeStatements(String[] statements);

    void executeScript(String scriptPath);

    String getExecutorId();

    /**
     * compares dataset from executor's database connection with a given dataset
     * @param expected configuration
     * @param ignoreCols name of column to ignore
     * @throws DatabaseUnitException if current dataset is not equal current dataset
     */
    void compareCurrentDataSetWith(DataSetConfig expected, String[] ignoreCols) throws DatabaseUnitException;

    /**
     * compares dataset from executor's database connection with a given dataset
     * @param expected configuration
     * @param ignoreCols name of column to ignore
     * @param replacers implementations of {@link Replacer}, called during reading expected dataset before comparison
     * @throws DatabaseUnitException if current dataset is not equal current dataset
     */
    void compareCurrentDataSetWith(DataSetConfig expected, String[] ignoreCols, Class<? extends Replacer>[] replacers) throws DatabaseUnitException;

    void setDBUnitConfig(DBUnitConfig dbUnitConfig);

    DBUnitConfig getDBUnitConfig();

    RiderDataSource getRiderDataSource() throws SQLException;

    void enableConstraints() throws SQLException ;


}
