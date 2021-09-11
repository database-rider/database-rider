package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.script.ScriptEngineManagerWrapper;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import javax.script.ScriptEngine;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds support for script language (JSR 223) in table values.
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class ScriptableTable implements ITable {

    private static final Logger log = Logger.getLogger(ScriptableTable.class.getName());

    private final ScriptEngineManagerWrapper manager;

    private final ITable delegate;

    public ScriptableTable(ITable delegate) {
        this.delegate = delegate;
        manager = ScriptEngineManagerWrapper.getInstance();
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return delegate.getTableMetaData();
    }

    @Override
    public int getRowCount() {
        return delegate.getRowCount();
    }

    @Override
    public Object getValue(int row, String column) throws DataSetException {
        final Object value = delegate.getValue(row, column);
        if (manager.rowValueContainsScriptEngine(value)) {
            try {
                return manager.getScriptResult(value.toString());
            } catch (Exception e) {
                log.log(Level.WARNING, String.format("Could not evaluate script expression for table '%s', column '%s'. The original value will be used.", getTableMetaData().getTableName(), column), e);
            }
        }
        return value;
    }

}
