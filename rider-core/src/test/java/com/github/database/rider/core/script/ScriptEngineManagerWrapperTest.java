package com.github.database.rider.core.script;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.script.ScriptException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ScriptEngineManagerWrapperTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    private ScriptEngineManagerWrapper scriptEngineManager = ScriptEngineManagerWrapper.getInstance();


    @Test
    public void shouldGetJsScriptResult() throws ScriptException {
        Object scriptResult = ScriptEngineManagerWrapper.getInstance().getScriptResult("js: 1+1");
        assertThat(scriptResult).isEqualTo(2);
    }

    @Test
    public void shouldGetGroovyScriptResult() throws ScriptException {
        Object scriptResult = scriptEngineManager.getScriptResult("groovy: 1+1");
        assertThat(scriptResult).isEqualTo(2);
    }

    @Test
    public void shouldNotGetScriptResultFromUnknownEngine() throws ScriptException {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Could not find script engine by name 'kotlin'");
        scriptEngineManager.getScriptResult("kotlin: 1+1");
    }

    @Test
    public void shouldGetJsScriptAssert() throws ScriptException {
        String script = "js:(value > 0)";
        assertTrue(scriptEngineManager.getScriptAssert(script, 2));
        assertFalse(scriptEngineManager.getScriptAssert(script, 0));
        assertFalse(scriptEngineManager.getScriptAssert(script, -1));
    }

    @Test
    public void shouldGetGroovyScriptAssert() throws ScriptException {
        String script = "groovy:(value > 0)";
        assertTrue(scriptEngineManager.getScriptAssert(script, 2));
        assertFalse(scriptEngineManager.getScriptAssert(script, 0));
        assertFalse(scriptEngineManager.getScriptAssert(script, -1));
    }
}
