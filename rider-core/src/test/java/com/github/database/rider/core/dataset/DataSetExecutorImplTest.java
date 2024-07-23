package com.github.database.rider.core.dataset;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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
        URL resource = DataSetExecutorImpl.class.getResource("/folder with some%20 characters/users.jar");
        resource = new URL("jar", "localhost", "file:" + resource.getFile() + "!/users/user-script.sql");
        String[] statements = dse.readScriptStatements(resource);
        assertEquals(4, statements.length);
        assertEquals("DELETE FROM User", statements[0]);
        assertEquals("INSERT INTO USER VALUES (10,'user10')", statements[1]);
        assertEquals("INSERT INTO USER VALUES (20,'user20')", statements[2]);
        assertEquals("INSERT INTO USER VALUES (30,'user30')", statements[3]);
    }

    @Test
    public void shouldReadScriptFromFileURL() {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        URL resource = DataSetExecutorImpl.class.getResource("/folder with some%20 characters/user-script.sql");
        String[] statements = dse.readScriptStatements(resource);
        assertEquals(7, statements.length);
        assertEquals("DELETE FROM User", statements[0]);
        assertEquals("INSERT INTO USER VALUES (10,'user10')", statements[1]);
        assertEquals("INSERT INTO USER VALUES (20,'user20')", statements[2]);
        assertEquals("INSERT INTO USER VALUES (30,'user30')", statements[3]);
        assertEquals("INSERT INTO USER VALUES (40,'用户40')", statements[4]);
        assertEquals("INSERT INTO USER VALUES (50, \"用户;50\")", statements[5]);
        assertEquals("INSERT INTO USER VALUES (60, \"user60\"), (70, \"user;70\")", statements[6]);
    }

    @Test
    public void shouldLoadDatasetsFromURL() throws IOException, DataSetException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        URL resourceXml = getClass().getResource("/datasets/xml/users.xml");
        IDataSet iDataSetFromXml = dse.loadDataSet(resourceXml.toString());
        String[] tableNamesXml = iDataSetFromXml.getTableNames();
        assertEquals("USER", tableNamesXml[0]);
        assertEquals("TWEET", tableNamesXml[1]);
        assertEquals("FOLLOWER", tableNamesXml[2]);

        URL resourceJson = getClass().getResource("/datasets/json/users.json");
        IDataSet iDataSetFromJson = dse.loadDataSet(resourceJson.toString());
        String[] tableNamesJson = iDataSetFromJson.getTableNames();
        assertEquals("USER", tableNamesJson[0]);
        assertEquals("TWEET", tableNamesJson[1]);
        assertEquals("FOLLOWER", tableNamesJson[2]);

        URL resourceXls = getClass().getResource("/datasets/xls/users.xls");
        IDataSet iDataSetFromXls = dse.loadDataSet(resourceXls.toString());
        String[] tableNamesXls = iDataSetFromXls.getTableNames();
        assertEquals("USER", tableNamesXls[0]);
        assertEquals("TWEET", tableNamesXls[1]);
        assertEquals("FOLLOWER", tableNamesXls[2]);

        URL resourceXlsx = getClass().getResource("/datasets/xlsx/users.xlsx");
        IDataSet iDataSetFromXlsx = dse.loadDataSet(resourceXlsx.toString());
        String[] tableNamesXlsx = iDataSetFromXlsx.getTableNames();
        assertEquals("USER", tableNamesXlsx[0]);
        assertEquals("TWEET", tableNamesXlsx[1]);
        assertEquals("FOLLOWER", tableNamesXlsx[2]);

        URL resourceYML = getClass().getResource("/datasets/yml/users.yml");
        IDataSet iDataSetFromYml = dse.loadDataSet(resourceYML.toString());
        String[] tableNamesYml = iDataSetFromYml.getTableNames();
        assertEquals("USER", tableNamesYml[0]);
        assertEquals("TWEET", tableNamesYml[1]);
        assertEquals("FOLLOWER", tableNamesYml[2]);

        URL resourceCvs = getClass().getResource("/datasets/csv/USER.csv");
        IDataSet iDataSetFromCsv = dse.loadDataSet(resourceCvs.toString());
        String[] tableNamesCsv = iDataSetFromCsv.getTableNames();
        assertEquals("USER", tableNamesCsv[0]);
        assertEquals("TWEET", tableNamesCsv[1]);
        assertEquals("FOLLOWER", tableNamesCsv[2]);
    }

    @Test
    public void shouldLoadDatasetsAsAResourceFromClasspath() throws IOException, DataSetException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        IDataSet iDataSetFromXml = dse.loadDataSet("/datasets/xml/users.xml");
        String[] tableNamesXml = iDataSetFromXml.getTableNames();
        assertEquals("USER", tableNamesXml[0]);
        assertEquals("TWEET", tableNamesXml[1]);
        assertEquals("FOLLOWER", tableNamesXml[2]);



        IDataSet iDataSetFromCsv = dse.loadDataSet("/datasets/csv/USER.csv");
        String[] tableNamesCsv = iDataSetFromCsv.getTableNames();
        assertEquals("USER", tableNamesCsv[0]);
        assertEquals("TWEET", tableNamesCsv[1]);
        assertEquals("FOLLOWER", tableNamesCsv[2]);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailLoadingDatasetFromHttp() throws IOException, DataSetException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        dse.loadDataSet("http://somewhere.de/test.csv");
    }

    @Test
    public void shouldLoadDatasetFromHttp () throws IOException, DataSetException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        IDataSet iDataSetFromXml = dse.loadDataSet("https://raw.githubusercontent.com/database-rider/database-rider/master/rider-core/src/test/resources/datasets/xml/users.xml");
        String[] tableNamesXml = iDataSetFromXml.getTableNames();
        assertEquals("USER", tableNamesXml[0]);
        assertEquals("TWEET", tableNamesXml[1]);
        assertEquals("FOLLOWER", tableNamesXml[2]);

    }

    @Test
    public void shouldIgnoreJsonSchemaProperty() throws IOException, DataSetException {
        DataSetExecutorImpl dse = DataSetExecutorImpl.instance(new ConnectionHolder() {
            private static final long serialVersionUID = 1L;

            @Override
            public Connection getConnection() {
                return null;
            }
        });
        URL resourceJson = getClass().getResource("/datasets/json/users_with_schema.json");
        IDataSet iDataSetFromJson = dse.loadDataSet(resourceJson.toString());
        String[] tableNamesJson = iDataSetFromJson.getTableNames();
        assertThat(tableNamesJson).hasSize(3);
        assertEquals("USER", tableNamesJson[0]);
        assertEquals("TWEET", tableNamesJson[1]);
        assertEquals("FOLLOWER", tableNamesJson[2]);
    }

}
