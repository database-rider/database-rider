package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.leak.LeakHunterException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.persistence.Persistence;
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

//tag::leak-hunter-declare[]
@RunWith(JUnit4.class)
@DBUnit(leakHunter = true) //<1>
public class LeakHunterIt {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(new ConnectionHolderImpl(getConnection()));

    @Rule
    public ExpectedException exception = ExpectedException.none();

    Connection connection;
    Connection connection2;

//end::leak-hunter-declare[]

    @BeforeClass
    public static void initDB() {
        //trigger db initialization
        Persistence.createEntityManagerFactory("rules-it");
    }

    @After
    public void closeConnections() throws SQLException {
        closeConnection(connection);
        closeConnection(connection2);
    }

    private void closeConnection(Connection connection) throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

//tag::find-leak[]

    @Test
    @DataSet("yml/user.yml")
    public void shouldFindTwoConnectionLeaks() throws SQLException {
        exception.expect(LeakHunterException.class);
        exception.expectMessage("Execution of method shouldFindTwoConnectionLeaks left 2 open connection(s).");
        createLeak(connection);
        createLeak(connection2);
    }
//end::find-leak[]

    @Test
    @DataSet("yml/user.yml")
    @DBUnit(leakHunter = false)
    public void shouldNotFindConnectionLeakWhenHunterIsDisabled() throws SQLException {
        createLeak(connection);
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

//tag::create-leak[]

    private void createLeak(Connection conn) throws SQLException {
        conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from user");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        }
    }
//end::create-leak[]

    private void createAndCloseConnection() throws SQLException {
        try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from user");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        }
    }


}
