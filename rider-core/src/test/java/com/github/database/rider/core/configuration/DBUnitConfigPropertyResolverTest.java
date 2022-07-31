package com.github.database.rider.core.configuration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.database.rider.core.configuration.DBUnitConfigPropertyResolver.resolveProperty;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DBUnitConfigPropertyResolverTest {

    @Rule
    public final EnvironmentVariables env = new EnvironmentVariables();

    @Before
    public void cleanProperties() {
      System.clearProperty("datasource.username");
      env.clear("datasource.username");
    }

    @Test
    public void shouldResolveSystemProperty() {
        System.setProperty("datasource.username", "sa");
        assertEquals("sa", resolveProperty("${datasource.username}"));
    }

    @Test
    public void shouldResolveEnvVar() {
        env.set("datasource.username", "envsa");
        assertEquals("envsa", resolveProperty("${datasource.username}"));
    }

    @Test
    public void shouldResolveSystemPropertyWhenEnvVarIsAlsoPresent() {
        System.setProperty("datasource.username", "sa");
        env.set("datasource.username", "envsa");
        assertEquals("sa", resolveProperty("${datasource.username}"));
    }

    @Test
    public void shouldReturnOriginalValueWhenELExpressionIsNotProvided() {
        System.setProperty("datasource.username", "sa");
        assertEquals("datasource.username", resolveProperty("datasource.username"));
    }


}