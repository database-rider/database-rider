package com.github.database.rider.core.script;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.script.ScriptException;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class ScriptEngineManagerWrapperTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    private ScriptEngineManagerWrapper scriptEngineManager = ScriptEngineManagerWrapper.getInstance();
    private SoftAssertions softly = new SoftAssertions();


    @Before
    public void init() {
        softly = new SoftAssertions();
    }

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
    public void shouldAssertValueGreaterThanZero() throws ScriptException {
        String script = "js:(value > 0)";
        softly.assertThat(scriptEngineManager.getScriptAssert(script, 2)).as("js script with value=2").isTrue();
        softly.assertThat(scriptEngineManager.getScriptAssert(script, 0)).as("js script with value=0").isFalse();
        softly.assertThat(scriptEngineManager.getScriptAssert(script, -1)).as("js script with value=-1").isFalse();
        script = "groovy:(value > 0)";
        softly.assertThat(scriptEngineManager.getScriptAssert(script, 2)).as("groovy script with value=2").isTrue();
        softly.assertThat(scriptEngineManager.getScriptAssert(script, 0)).as("groovy script with value=0").isFalse();
        softly.assertThat(scriptEngineManager.getScriptAssert(script, -1)).as("groovy script with value=-1").isFalse();
        softly.assertAll();
    }

    @Test
    public void shouldAssertNullValue() throws ScriptException {
        SoftAssertions soft = new SoftAssertions();
        String script = "js:(value == null)";
        soft.assertThat(scriptEngineManager.getScriptAssert(script, null)).as("js script with null value").isTrue();
        soft.assertThat(scriptEngineManager.getScriptAssert(script, 1)).as("js script with non-null value").isFalse();
        script = "groovy:(value == null)";
        soft.assertThat(scriptEngineManager.getScriptAssert(script, null)).as("groovy script with null value").isTrue();
        soft.assertThat(scriptEngineManager.getScriptAssert(script, 1)).as("groovy script with non-null value").isFalse();
        soft.assertAll();
    }

    @Test
    public void shouldAssertContainsValue() throws ScriptException {
        SoftAssertions soft = new SoftAssertions();
        String script = "js:(value.contains('dbunit'))";
        soft.assertThat(scriptEngineManager.getScriptAssert(script, "dbunit rules")).as("js script with 'dbunit rules' value").isTrue();
        soft.assertThat(scriptEngineManager.getScriptAssert(script, "database rider rules")).as("js script 'database rider' value").isFalse();
        script = "groovy:(value.contains('dbunit'))";
        soft.assertThat(scriptEngineManager.getScriptAssert(script, "dbunit rules")).as("groovy script with 'dbunit rules' value").isTrue();
        soft.assertThat(scriptEngineManager.getScriptAssert(script, "database rider rules")).as("groovy script 'database rider' value").isFalse();
        soft.assertAll();
    }

    @Test
    public void shouldNotAssertInvalidScript() throws ScriptException {
        exceptionRule.expect(ScriptException.class);
        exceptionRule.expectMessage("value.includes is not a function");
        String script = "js:(value.includes('dbunit'))";
        scriptEngineManager.getScriptAssert(script, "dbunit rules");
    }
}
