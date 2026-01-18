package com.github.database.rider.core.util;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface ContainsErrorMessage {

    Logger logger = LoggerFactory.getLogger(ContainsErrorMessage.class);

    static ContainsErrorMessage create(List<String> columnNames, ITable originalTable) throws DataSetException {
        if (logger.isErrorEnabled()) {
            return new DefaultContainsErrorMessage(columnNames, originalTable);
        }
        return DummyContainsErrorMessage.INSTANCE;
    }

    void initWithValues(List<Object> values) throws DataSetException;

    void addTableHeader();

    void addRow(int row) throws DataSetException;

    void addFail(int column, int row) throws DataSetException;

    void setMatch();

    void nextLine();

    void print();
}
