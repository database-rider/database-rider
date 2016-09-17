package com.github.database.rider.core.replacer;

import org.dbunit.dataset.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ScriptReplacer {

    private static ScriptReplacer instance;

    private Pattern scriptEnginePattern = Pattern.compile("^[a-zA-Z]+:.+");//any non digit char followed by ':' followed by 1 or more chars (eg: js: new Date().toString()

    private static Logger log = Logger.getLogger(ScriptReplacer.class.getName());

    private Map<String, ScriptEngine> engines;

    private ScriptEngineManager manager;

    private ScriptReplacer() {
        engines = new HashMap<>();
        manager = new ScriptEngineManager();
    }

    public static IDataSet replace(IDataSet dataset) {
        if (instance == null) {
            instance = new ScriptReplacer();
        }
        ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataset);
        try {
            instance.replaceScripts(replacementDataSet);
        } catch (DataSetException e) {
            log.log(Level.WARNING, "Could not replace dataset: " + dataset, e);
        }
        return replacementDataSet;
    }

    private void replaceScripts(ReplacementDataSet dataSet) throws DataSetException {
        ITableIterator iterator = dataSet.iterator();
        while (iterator.next()) {
            ITable table = iterator.getTable();
            for (Column column : table.getTableMetaData().getColumns()) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    Object value = table.getValue(i, column.getColumnName());
                    if (value == null) {
                        continue;
                    }
                    if (scriptEnginePattern.matcher(value.toString()).matches()) {
                        ScriptEngine engine = getScriptEngine(value.toString());
                        if (engine != null) {
                            Object scriptResult = getScriptResult(value.toString(), engine);
                            if (scriptResult != null) {
                                dataSet.addReplacementObject(value, scriptResult);
                            } else {
                                throw new RuntimeException(String.format("Could not perform script replacement for table '%s', column '%s'.", table.getTableMetaData().getTableName(), column.getColumnName()));
                            }
                        }

                    }
                }
            }
        }
    }

    private ScriptEngine getScriptEngine(String value) {
        String engineName = value.substring(0, value.indexOf(":"));
        if (engines.containsKey(engineName)) {
            return engines.get(engineName);
        } else {
            ScriptEngine engine = manager.getEngineByName(engineName);
            if (engine != null) {
                engines.put(engineName, engine);
            } else {
                log.warning(String.format("Could not find script engine with name %s in classpath", engineName));
            }
            return engine;
        }

    }

    private Object getScriptResult(String script, ScriptEngine engine) {
        String scriptToExecute = script.substring(script.indexOf(":") + 1);
        try {
            return engine.eval(scriptToExecute);
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not perform replacement for script: " + script, e);
            return null;
        }
    }


}
