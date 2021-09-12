package com.github.database.rider.core.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ScriptEngineManagerWrapper {

    private static final ScriptEngineManagerWrapper INSTANCE = new ScriptEngineManagerWrapper();
    private static final Pattern SCRIPT_EXPRESSION_PATTERN = Pattern.compile("\\b((js|groovy):\\((.*value.*)\\))");//starts with js or groovy followed by ':' followed by anything before or after the 'value' inside parentheses

    private final ScriptEngineManager manager;

    private final Map<String, ScriptEngine> engines;

    private ScriptEngineManagerWrapper() {
        engines = new HashMap<>();
        manager = new ScriptEngineManager();
    }

    public static ScriptEngineManagerWrapper getInstance() {
        return INSTANCE;
    }

    public boolean rowValueContainsScriptEngine(Object value) {
        if (value == null || value.toString().length() == 0) {
            return false;
        }
        final String rowValueLowerCase = value.toString().toLowerCase();
        return rowValueLowerCase.startsWith("js:") || rowValueLowerCase.startsWith("groovy:");
    }

    /**
     * Evaluates the script expression
     *
     * @return script expression result
     */
    public Object getScriptResult(String script) throws ScriptException {
        final ScriptEngine engine = getScriptEngine(script.trim());
        String scriptToExecute = getScriptToExecute(script);
        return engine.eval(scriptToExecute);
    }

    public boolean getScriptAssert(final String script, final Object actualValue) throws ScriptException {
        final ScriptEngine engine = getScriptEngine(script.trim());
        final String scriptToExecute = getScriptToExecute(script);
        engine.put("value", actualValue);
        Object result = engine.eval(scriptToExecute);
        return Boolean.parseBoolean(result.toString());
    }

    /**
     * Parses table cell to get script engine
     *
     * @param value the table cell
     * @return scriptEngine
     */
    private ScriptEngine getScriptEngine(final String value) {
        final String engineName = value.substring(0, value.indexOf(':'));
        if (engines.containsKey(engineName)) {
            return engines.get(engineName);
        } else {
            ScriptEngine engine = manager.getEngineByName(engineName);
            if (engine == null) {
                throw new RuntimeException(String.format("Could not find script engine by name '%s'", engineName));
            }
            engines.put(engineName, engine);
            return engine;
        }
    }

    private String getScriptToExecute(String script) {
        return script.substring(script.indexOf(':') + 1);
    }

    public boolean hasScriptExpression(Object script) {
        if (script == null) {
            return false;
        }
        return SCRIPT_EXPRESSION_PATTERN.matcher(script.toString()).matches();
    }
}
