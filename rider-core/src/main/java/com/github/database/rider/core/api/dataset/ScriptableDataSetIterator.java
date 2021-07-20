package com.github.database.rider.core.api.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class ScriptableDataSetIterator implements ITableIterator {

    private ITableIterator delegate;

    public ScriptableDataSetIterator(ITableIterator delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean next() throws DataSetException {
        return delegate.next();
    }

    @Override
    public ITableMetaData getTableMetaData() throws DataSetException {
        return delegate.getTableMetaData();
    }

    @Override
    public ITable getTable() throws DataSetException {
        return new ScriptableTable(delegate.getTable());
    }


}