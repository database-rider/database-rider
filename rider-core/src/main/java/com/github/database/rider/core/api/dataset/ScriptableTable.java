package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.script.ScriptEngineManagerWrapper;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import static java.lang.String.format;

/**
 * Adds support for script language (JSR 223) in table values.
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class ScriptableTable implements ITable {

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
        if (manager.rowValueContainsScriptEngine(value) && !manager.hasScriptExpression(value)) {
            try {
                return manager.getScriptResult(value.toString());
            } catch (Exception e) {
                throw new RuntimeException(format("Could not evaluate script expression: '%s' for table '%s', column '%s'.", value, getTableMetaData().getTableName(), column), e);
            }
        }
        return value;
    }

}
