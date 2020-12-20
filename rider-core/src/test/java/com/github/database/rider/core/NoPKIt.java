package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import org.flywaydb.core.Flyway;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.SQLException;

public class NoPKIt {
    private static String DB_URL = "jdbc:hsqldb:mem:nopk;DB_CLOSE_DELAY=-1";

    private static Flyway flyway;

    private static Connection connection;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.
            instance(new ConnectionHolderImpl(connection));

    public NoPKIt() throws SQLException {
    }

    @BeforeClass
    public static void initMigration() throws SQLException {
        flyway = new Flyway();
        flyway.setDataSource(DB_URL, "sa", "");
        flyway.setLocations("filesystem:src/test/resources/migration");
        flyway.migrate();
        connection = flyway.getDataSource().getConnection();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        flyway.clean();
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    //@DataSet(value = "yml/nopk.yml", cleanBefore = true, strategy = SeedStrategy.REFRESH)
    public void shouldNotSeedTableWithoutPK() {
        exception.expect(DataBaseSeedingException.class);
        exception.expectMessage("Could not initialize dataset: yml/nopk.yml");
        DataSetExecutorImpl.instance(new ConnectionHolderImpl(connection))
                .createDataSet(new DataSetConfig("yml/nopk.yml").strategy(SeedStrategy.REFRESH));
    }

    @Test
    @DataSet(value = "yml/nopk.yml", cleanBefore = true, strategy = SeedStrategy.REFRESH)
    @DBUnit(disablePKCheckFor = "NOPK")
    public void shouldSeedTableWithoutPK() {
    }

}
