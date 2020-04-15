package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.leak.LeakHunterException;
import com.github.database.rider.core.model.lowercase.*;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@RunWith(JUnit4.class)
@DBUnit(caseSensitiveTableNames = true, escapePattern = "\"?\"")
public class PostgreSQLDatabaseIt {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:9.4.20");

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("postgre-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());
    
    @Rule
    public ExpectedException exception = ExpectedException.none();


    @BeforeClass
    public static void setupContainer() {
        postgres.start();
    }

    @AfterClass
    public static void shutdown() {
        postgres.stop();
    }

    @Test
    @DataSet(value = "datasets/yml/lowercaseUsers.yml")
    public void shouldSeedDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }
    
    @Test
    @DataSet(value = "datasets/yml/lowercaseTweet.yml", disableConstraints = true)
    public void shouldSeedDataSetDisablingConstraints() {
        Tweet tweet = EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = 'abcdef12345'", Tweet.class).getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isEqualTo("dbrider rules!");
    }

    @Test
    @DataSet(value = "datasets/yml/lowercaseUsers.yml", tableOrdering = {"user","tweet","follower"}, cleanBefore = true, cleanAfter = true)
    public void shouldListUsersWithCaseInSensitiveTableNamesAndTableOrdering() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DataSet(value = "datasets/yml/lowercaseUsers.yml")
    @DBUnit(leakHunter = true, caseSensitiveTableNames = true, escapePattern = "\"?\"")
    public void shouldFindConnectionLeak() throws SQLException {
        exception.expect(LeakHunterException.class);
        exception.expectMessage("Execution of method shouldFindConnectionLeak left 1 open connection(s).");
        createLeak();
    }

    private void createLeak() throws SQLException {
        //Connection connection = emProvider.connection(); //entityManager connections won't leak
    	Connection connection = getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from \"user\"");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        }
    }
    
    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:tc:postgresql://localhost/test", "test", "test");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
