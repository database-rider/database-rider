package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.leak.LeakHunterException;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.testcontainers.containers.MySQLContainer;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class MySQLDatabaseIt {

    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.6")
            .withUsername("test").withPassword("test");

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("mysql-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());
    
    @Rule
    public ExpectedException exception = ExpectedException.none();


    @BeforeClass
    public static void setupContainer() {
        mysql.start();
    }

    @AfterClass
    public static void shutdown() {
        mysql.stop();
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml")
    public void shouldSeedDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }
    
    @Test
    @DataSet(value = "datasets/yml/tweet.yml", disableConstraints = true)
    public void shouldSeedDataSetDisablingConstraints() {
        Tweet tweet = EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = 'abcdef12345'", Tweet.class).getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isEqualTo("dbunit rules again!");
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml")
    @DBUnit(leakHunter = true)
    public void shouldFindConnectionLeak() throws SQLException {
        exception.expect(LeakHunterException.class);
        exception.expectMessage("Execution of method shouldFindConnectionLeak left 1 open connection(s).");
        createLeak();
    }
    
    private void createLeak() throws SQLException {
        //Connection connection = emProvider.connection(); //entityManager connections won't leak
    	Connection connection = getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select count(*) from USER");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
        }
    }
    
    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:tc:mysql:5.6://localhost/test", "test", "test");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
