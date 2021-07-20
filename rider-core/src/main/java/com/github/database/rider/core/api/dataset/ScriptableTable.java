package com.github.database.rider.core.api.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds support for script language (JSR 223) in table values.
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class ScriptableTable implements ITable {

    private static final Logger log = Logger.getLogger(ScriptableTable.class.getName());

    private final ScriptEngineManager manager;

    private final Map<String, ScriptEngine> engines;

    private final ITable delegate;

    public ScriptableTable(ITable delegate) {
        this.delegate = delegate;
        engines = new HashMap<>();
        manager = new ScriptEngineManager();
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
        if (rowValueContainsScriptEngine(value)) {
            ScriptEngine engine = getScriptEngine(value.toString().trim());
            if (engine != null) {
                try {
                    return getScriptResult(value.toString(), engine);
                } catch (Exception e) {
                    log.log(Level.WARNING, String.format("Could not evaluate script expression for table '%s', column '%s'. The original value will be used.", getTableMetaData().getTableName(), column), e);
                }
            }
        }
        return value;
    }

    private boolean rowValueContainsScriptEngine(Object value) {
        if (value == null || value.toString().length() == 0) {
            return false;
        }
        final String rowValueLowerCase = value.toString().toLowerCase();
        return rowValueLowerCase.startsWith("js:") || rowValueLowerCase.startsWith("groovy:");
    }

    /**
     * Parses table cell to get script engine
     *
     * @param value the table cell
     * @return scriptEngine
     */
    private ScriptEngine getScriptEngine(String value) {
        String engineName = value.substring(0, value.indexOf(':'));
        if (engines.containsKey(engineName)) {
            return engines.get(engineName);
        } else {
            ScriptEngine engine = manager.getEngineByName(engineName);
            if (engine != null) {
                engines.put(engineName, engine);
            } else {
                log.warning(String.format("Could not find script engine by name '%s'", engineName));
            }
            return engine;
        }
    }

    /**
     * Evaluates the script expression
     *
     * @return script expression result
     */
    private Object getScriptResult(String script, ScriptEngine engine) throws ScriptException {
        String scriptToExecute = script.substring(script.indexOf(':') + 1);
        return engine.eval(scriptToExecute);
    }

}
