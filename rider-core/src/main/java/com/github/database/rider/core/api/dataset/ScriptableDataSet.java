package com.github.database.rider.core.api.dataset;

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;

public class ScriptableDataSet extends AbstractDataSet {

    private IDataSet delegate;

    public ScriptableDataSet(boolean caseSensitiveTableNames, IDataSet delegate) {
        super(caseSensitiveTableNames);
        this.delegate = delegate;
    }

    @Override
    protected ITableIterator createIterator(boolean reversed) throws DataSetException {
        return new ScriptableDataSetIterator(reversed ? delegate.reverseIterator() : delegate.iterator());
    }

}
