package com.github.database.rider;

import com.github.database.rider.leak.LeakHunterException;
import com.github.database.rider.api.configuration.DBUnit;
import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.connection.ConnectionHolderImpl;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.persistence.Persistence;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DBUnit(leakHunter = true)
public class LeakHunterIt {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(new ConnectionHolderImpl(getConnection()));

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @BeforeClass
    public static void initDB() {
        //trigger db initialization
        Persistence.createEntityManagerFactory("rules-it");
    }


    @Test
    @DataSet("yml/user.yml")
    public void shouldFindConnectionLeak() throws SQLException {
        exception.expect(LeakHunterException.class);
        exception.expectMessage("Execution of method shouldFindConnectionLeak left 1 open connection(s).");
        createLeak();
    }

    @Test
    @DataSet("yml/user.yml")
    public void shouldFindTwoConnectionLeaks() throws SQLException {
        exception.expect(LeakHunterException.class);
        exception.expectMessage("Execution of method shouldFindTwoConnectionLeaks left 2 open connection(s).");
        createLeak();
        createLeak();
    }


    @Test
    @DataSet("yml/user.yml")
    @DBUnit(leakHunter = false)
    public void shouldNotFindConnectionLeakWhenHunterIsDisabled() throws SQLException {
        createLeak();
    }

    @Test
    @DataSet("yml/user.yml")
    public void shouldNotFindConnectionLeakWhenConnectionIsClosed() throws SQLException {
        createAndCloseConnection();
    }


    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
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


}
