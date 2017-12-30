package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;

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
                .hasFieldOrPropertyWithValue("caseInsensitiveStrategy", Orthography.UPPERCASE);

        assertThat(config.getProperties()).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", false).
                containsEntry("caseSensitiveTableNames", false).
                containsEntry("batchSize", 100).
                containsEntry("fetchSize", 100).
                containsEntry("allowEmptyFields", false).
                doesNotContainKey("escapePattern");

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
                .hasFieldOrPropertyWithValue("caseInsensitiveStrategy", Orthography.UPPERCASE);

        assertThat(config.getProperties()).
                containsEntry("allowEmptyFields", true).
                containsEntry("batchedStatements", true).
                containsEntry("qualifiedTableNames", true).
                containsEntry("batchSize", 200).
                containsEntry("fetchSize", 200).
                containsEntry("escapePattern", "[?]");
    }

    @Test
    public void shouldMergeDBUnitConfigViaCustomGlobalFile() throws IOException {
        copyResourceToFile("/config/merge-dbunit.yml", customConfigFile);

        DBUnitConfig config = GlobalConfig.newInstance().getDbUnitConfig();

        assertThat(config).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", true)
                .hasFieldOrPropertyWithValue("cacheTableNames", true)
                .hasFieldOrPropertyWithValue("leakHunter", true)
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
    @DBUnit(cacheTableNames = false, allowEmptyFields = true, batchSize = 50)
    public void shouldLoadDBUnitConfigViaAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDBUnitConfigViaAnnotation");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        assertThat(dbUnitConfig).isNotNull()
                .hasFieldOrPropertyWithValue("cacheConnection", true)
                .hasFieldOrPropertyWithValue("cacheTableNames", false);

        assertThat(dbUnitConfig.getProperties()).
                containsEntry("allowEmptyFields", true).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", false).
                containsEntry("batchSize", 50).
                containsEntry("fetchSize", 100).
                doesNotContainKey("escapePattern");
    }
}
