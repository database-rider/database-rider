package com.github.database.rider.cdi;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.core.leak.LeakHunterException;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiTestRunner.class)
@DBUnit(leakHunter = true)
@DBUnitInterceptor
public class LeakHunterCDIIt {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void initDB() {
        EntityManagerProvider.instance("cdipu");//just to initialize db
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:hsqldb:mem:test-cdi;DB_CLOSE_DELAY=-1", "sa", "");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createLeak() throws SQLException {
        Connection connection = getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from user");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        }
    }

    private void createAndCloseConnection() throws SQLException {
        Connection connection = getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from user");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        } finally {
            connection.close();
        }
    }


    @Test(expected = LeakHunterException.class)
    @DataSet("yml/users.yml")
    public void shouldFindConnectionLeak() throws SQLException {
        createLeak();
    }

    @Test(expected = LeakHunterException.class)
    @DataSet("yml/users.yml")
    public void shouldFindTwoConnectionLeaks() throws SQLException {
        createLeak();
        createLeak();
    }


    @Test
    @DataSet("yml/users.yml")
    @DBUnit(leakHunter = false)
    public void shouldNotFindConnectionLeakWhenHunterIsDisabled() throws SQLException {
        createLeak();
    }

    @Test
    @DataSet("yml/users.yml")
    public void shouldNotFindConnectionLeakWhenConnectionIsClosed() throws SQLException {
        createAndCloseConnection();
    }


}
