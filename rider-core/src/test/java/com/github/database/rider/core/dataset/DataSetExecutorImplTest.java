package com.github.database.rider.core.dataset;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import org.junit.Test;

import com.github.database.rider.core.api.connection.ConnectionHolder;

public class DataSetExecutorImplTest {

    @Test
    public void shouldReadScriptFromJarURL() throws MalformedURLException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        // simulate URL resource read from jar file in classpath
        URL resource = DataSetExecutorImpl.class.getResource("/scripts/users.jar");
        resource = new URL("jar", "localhost", "file:" + resource.getFile() + "!/users/user-script.sql");
        String[] statements = dse.readScriptStatements(resource);
        assertEquals(4, statements.length);
        assertEquals("DELETE FROM User", statements[0]);
        assertEquals("INSERT INTO USER VALUES (10,'user10')", statements[1]);
        assertEquals("INSERT INTO USER VALUES (20,'user20')", statements[2]);
        assertEquals("INSERT INTO USER VALUES (30,'user30')", statements[3]);
    }
}
