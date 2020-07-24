package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.replacers.CustomReplacer;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.dataset.datatype.DataType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 03/09/16.
 */
@RunWith(JUnit4.class)
public class DBUnitConfigTest {

    private File customConfigFile = new File("target/test-classes/dbunit.yml");

    @After
    public void deleteConfigFile() {
        customConfigFile.delete();
    }

    @Test
    public void shouldInitDBUnitConfigWithDefaultValues() {
        DBUnitConfig config = GlobalConfig.newInstance().getDbUnitConfig();

        assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", true)
                .hasFieldOrPropertyWithValue("cacheTableNames", true)
                .hasFieldOrPropertyWithValue("leakHunter", false)
                .hasFieldOrPropertyWithValue("raiseExceptionOnCleanUp", false)
                .hasFieldOrPropertyWithValue("expectedDbType", RiderDataSource.DBType.UNKNOWN)
                .hasFieldOrPropertyWithValue("caseInsensitiveStrategy", Orthography.UPPERCASE);

        assertThat(config.getProperties()).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", false).
                containsEntry("schema", null).
                containsEntry("caseSensitiveTableNames", false).
                containsEntry("batchSize", 100).
                containsEntry("fetchSize", 100).
                containsEntry("allowEmptyFields", false).
                containsKey("replacers").
                doesNotContainKey("escapePattern").
                doesNotContainKey("datatypeFactory");

        assertThat(config.getConnectionConfig()).isNotNull()
                .hasFieldOrPropertyWithValue("driver", "")
                .hasFieldOrPropertyWithValue("url", "")
                .hasFieldOrPropertyWithValue("user", "")
                .hasFieldOrPropertyWithValue("password", "");
    }

    private void copyResourceToFile(String resourceName, File to) throws IOException {
        try (InputStream from = getClass().getResourceAsStream(resourceName)) {
            Files.copy(from, to.toPath());
        }
    }

    @Test
    public void shouldLoadDBUnitConfigViaCustomGlobalFile() throws IOException {
        copyResourceToFile("/config/sample-dbunit.yml", customConfigFile);

        DBUnitConfig config = GlobalConfig.newInstance().getDbUnitConfig();

        assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", false)
                .hasFieldOrPropertyWithValue("cacheTableNames", false)
                .hasFieldOrPropertyWithValue("leakHunter", true)
                .hasFieldOrPropertyWithValue("raiseExceptionOnCleanUp", true)
                .hasFieldOrPropertyWithValue("expectedDbType", RiderDataSource.DBType.HSQLDB)
                .hasFieldOrPropertyWithValue("caseInsensitiveStrategy", Orthography.UPPERCASE);

        assertThat(config.getProperties()).
                containsEntry("allowEmptyFields", true).
                containsEntry("batchedStatements", true).
                containsEntry("qualifiedTableNames", true).
                containsEntry("schema", "public").
                containsEntry("batchSize", 200).
                containsEntry("fetchSize", 200).
                containsEntry("escapePattern", "[?]").
                containsEntry("datatypeFactory", new MockDataTypeFactory()).
                containsEntry("replacers", new ArrayList<>(Arrays.asList(new CustomReplacer())));
    }

    @Test
    public void shouldMergeDBUnitConfigViaCustomGlobalFile() throws IOException {
        copyResourceToFile("/config/merge-dbunit.yml", customConfigFile);

        DBUnitConfig config = GlobalConfig.newInstance().getDbUnitConfig();

        assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", true)
                .hasFieldOrPropertyWithValue("cacheTableNames", true)
                .hasFieldOrPropertyWithValue("leakHunter", true)
                .hasFieldOrPropertyWithValue("raiseExceptionOnCleanUp", false)
                .hasFieldOrPropertyWithValue("expectedDbType", RiderDataSource.DBType.UNKNOWN)
                .hasFieldOrPropertyWithValue("caseInsensitiveStrategy", Orthography.UPPERCASE);

        assertThat(config.getProperties()).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", true).
                containsEntry("caseSensitiveTableNames", false).
                containsEntry("batchSize", 100).
                containsEntry("fetchSize", 200).
                containsEntry("allowEmptyFields", false).
                containsEntry("escapePattern", "[?]");
    }

    @Test
    @DBUnit(cacheTableNames = false, allowEmptyFields = true, batchSize = 50, schema = "public",
            expectedDbType = RiderDataSource.DBType.HSQLDB)
    public void shouldLoadDBUnitConfigViaAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDBUnitConfigViaAnnotation");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        assertThat(dbUnitConfig).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", true)
                .hasFieldOrPropertyWithValue("cacheTableNames", false)
                .hasFieldOrPropertyWithValue("expectedDbType", RiderDataSource.DBType.HSQLDB);

        assertThat(dbUnitConfig.getProperties()).
                containsEntry("allowEmptyFields", true).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", false).
                containsEntry("schema", "public").
                containsEntry("batchSize", 50).
                containsEntry("fetchSize", 100).
                doesNotContainKey("escapePattern").
                doesNotContainKey("datatypeFactory");
    }

    @Test
    @DBUnit()
    public void shouldTreatAnnotationWithNonExistingSchemaAsNull() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldTreatAnnotationWithNonExistingSchemaAsNull");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        assertThat(dbUnitConfig.getProperties()).
                containsEntry("schema", null);
    }

    @Test
    @DBUnit(dataTypeFactoryClass = MockDataTypeFactory.class)
    public void shouldInstantiateDataTypeFactoryFromAnnotationIfSpecified() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldInstantiateDataTypeFactoryFromAnnotationIfSpecified");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        assertThat(dbUnitConfig.getProperties()).
                containsEntry("datatypeFactory", new MockDataTypeFactory());
    }
    
    @Test
    @DBUnit(metaDataHandler = MockMetadataHandler.class)
    public void shouldInstantiateMetadataHandlerFromAnnotationIfSpecified() throws NoSuchMethodException, SecurityException {
    	Method method = getClass().getMethod("shouldInstantiateMetadataHandlerFromAnnotationIfSpecified");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        assertThat(dbUnitConfig.getProperties()).
                containsEntry("metadataHandler", new MockMetadataHandler());
    }

    public static class MockDataTypeFactory implements org.dbunit.dataset.datatype.IDataTypeFactory {
        @Override
        public DataType createDataType(int i, String s) {
            throw new UnsupportedOperationException("only for configuration tests");
        }

        @Override
        public DataType createDataType(int i, String s, String s1, String s2) {
            throw new UnsupportedOperationException("only for configuration tests");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass());
        }
    }
    
    public static class MockMetadataHandler implements IMetadataHandler {

		@Override
		public ResultSet getColumns(DatabaseMetaData databaseMetaData, String schemaName, String tableName)
				throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");
		}

		@Override
		public boolean matches(ResultSet resultSet, String schema, String table, boolean caseSensitive)
				throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");		}

		@Override
		public boolean matches(ResultSet resultSet, String catalog, String schema, String table, String column,
				boolean caseSensitive) throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");
		}

		@Override
		public String getSchema(ResultSet resultSet) throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");
		}

		@Override
		public boolean tableExists(DatabaseMetaData databaseMetaData, String schemaName, String tableName)
				throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");
		}

		@Override
		public ResultSet getTables(DatabaseMetaData databaseMetaData, String schemaName, String[] tableTypes)
				throws SQLException {
			throw new UnsupportedOperationException("only for configuration tests");
		}

		@Override
		public ResultSet getPrimaryKeys(DatabaseMetaData databaseMetaData, String schemaName, String tableName)
				throws SQLException {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("only for configuration tests");
		}
		
       @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass());
        }		
    	
    }
    

}
