package com.github.database.rider.core.api.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

public interface DataSetProvider {

    IDataSet provide() throws DataSetException;

}
