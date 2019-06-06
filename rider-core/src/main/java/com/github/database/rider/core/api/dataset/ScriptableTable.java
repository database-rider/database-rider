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
import java.util.regex.Pattern;

/**
 * Adds support for script language (JSR 223) in table values.
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class ScriptableTable implements ITable {

    //any non digit char (except 'regex') followed by ':' followed by 1 or more chars e.g: js: new Date().toString()
    private static final Pattern scriptEnginePattern = Pattern.compile("^(?!regex)[a-zA-Z]+:.+");

    static Logger log = Logger.getLogger(ScriptableTable.class.getName());

    ScriptEngineManager manager;
    
    private Map<String, ScriptEngine> engines;

    private ITable delegate;


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
        Object value = delegate.getValue(row, column);
        if (value != null && scriptEnginePattern.matcher(value.toString()).matches()) {
            ScriptEngine engine = getScriptEngine(value.toString().trim());
            if (engine != null) {
                try {
                    return getScriptResult(value.toString(), engine);
                } catch (Exception e) {
                    log.log(Level.WARNING,String.format("Could not evaluate script expression for table '%s', column '%s'. The original value will be used.", getTableMetaData().getTableName(), column),e);
                }
            }
        }
        return value;
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
