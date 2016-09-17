package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.configuration.DataSetConfig;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
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

    ConnectionHolder getConnectionHolder();

    void setConnectionHolder(ConnectionHolder connectionHolder);

    void clearDatabase(DataSetConfig dataset) throws SQLException;

    void executeStatements(String[] statements);

    void executeScript(String scriptPath);

    String getExecutorId();

    /**
     * compares dataset from executor's databse connection with a given dataset
     * @param expected configuration
     * @throws DatabaseUnitException if current dataset is not equal current dataset
     */
    void compareCurrentDataSetWith(DataSetConfig expected, String[] ignoreCols) throws DatabaseUnitException;


    void setDBUnitConfig(DBUnitConfig dbUnitConfig);

    DBUnitConfig getDBUnitConfig();

    DatabaseConnection getDBUnitConnection();


}
