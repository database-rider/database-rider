package com.github.database.rider.core.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.GlobalConfig;
import com.github.database.rider.core.util.EntityManagerProvider;

/**
 * @author kerraway
 * @date 2020/07/24
 * @see RiderDataSource
 */
@RunWith(JUnit4.class)
public class RiderDataSourceIT {

    @Rule
    public final EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    private File customConfigFile;

    @Before
    public void setUp() {
        customConfigFile = new File("target/test-classes/dbunit.yml");
    }

    @After
    public void tearDown() {
        if (customConfigFile != null) {
            customConfigFile.delete();
        }
    }

    @Test
    public void shouldInitDBUnitConfigWithDefaultValues() {
        DBUnitConfig dbUnitConfig = GlobalConfig.newInstance().getDbUnitConfig();

        createRiderDataSourceAndAssertDbType(dbUnitConfig);
    }

    @Test
    public void shouldLoadDBUnitConfigViaCustomGlobalFile() throws IOException {
        copyResourceToFile("/config/sample-dbunit.yml", customConfigFile);
        DBUnitConfig dbUnitConfig = GlobalConfig.newInstance().getDbUnitConfig();

        createRiderDataSourceAndAssertDbType(dbUnitConfig);
    }

    @Test
    public void shouldMergeDBUnitConfigViaCustomGlobalFile() throws IOException {
        copyResourceToFile("/config/merge-dbunit.yml", customConfigFile);
        DBUnitConfig dbUnitConfig = GlobalConfig.newInstance().getDbUnitConfig();

        createRiderDataSourceAndAssertDbType(dbUnitConfig);
    }

    @Test
    @DBUnit(expectedDbType = RiderDataSource.DBType.HSQLDB)
    public void shouldLoadDBUnitConfigViaAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDBUnitConfigViaAnnotation");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        createRiderDataSourceAndAssertDbType(dbUnitConfig);
    }

    @Test(expected = RuntimeException.class)
    @DBUnit(expectedDbType = RiderDataSource.DBType.H2)
    public void shouldLoadDBUnitConfigViaAnnotationWithWrongDbType() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDBUnitConfigViaAnnotationWithWrongDbType");
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);

        createRiderDataSourceAndAssertDbType(dbUnitConfig);
    }

    private void createRiderDataSourceAndAssertDbType(final DBUnitConfig dbUnitConfig) {
        RiderDataSource riderDataSource = new RiderDataSource(new ConnectionHolder() {
            @Override
            public Connection getConnection() throws SQLException {
                return emProvider.connection();
            }
        }, dbUnitConfig);

        assertThat(riderDataSource).isNotNull()
                .hasFieldOrPropertyWithValue("dbType", RiderDataSource.DBType.HSQLDB);
    }

    private void copyResourceToFile(String resourceName, File to) throws IOException {
        try (InputStream from = getClass().getResourceAsStream(resourceName)) {
            Files.copy(from, to.toPath());
        }
    }
}