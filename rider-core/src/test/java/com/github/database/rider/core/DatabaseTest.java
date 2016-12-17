package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 23/07/15.
 */
@RunWith(JUnit4.class)
public class DatabaseTest {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(JDBCConnection());


    @Test
    @DataSet(value = "datasets/yml/user.yml")
    public void shouldSeedDatabase() {
        List<User> users = em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().
                hasSize(2);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", disableConstraints = true)
    public void shouldSeedDataSetDisablingContraints() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", executeStatementsBefore = "SET DATABASE REFERENTIAL INTEGRITY FALSE;")
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers join fetch u.tweets join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
    }


    @Test
    @DataSet(value = "datasets/yml/users.yml",
            useSequenceFiltering = false,
            tableOrdering = {"USER", "TWEET", "FOLLOWER"},
            executeStatementsBefore = {"DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"}//needed because other tests created user dataset
    )
    public void shouldSeedDataSetUsingTableCreationOrder() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u left join fetch u.tweets left join fetch u.followers").getResultList();
        assertThat(users).hasSize(2);
    }


    // tag::seedDatabase[]
    @Test
    @DataSet(value = "datasets/yml/users.yml", useSequenceFiltering = true)
    public void shouldSeedUserDataSet() {
        User user = (User) EntityManagerProvider.em().
                createQuery("select u from User u join fetch u.tweets join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).isNotNull().hasSize(1);
        Tweet tweet = user.getTweets().get(0);
        assertThat(tweet).isNotNull();
        Calendar date = tweet.getDate();
        Calendar now = Calendar.getInstance();
        assertThat(date.get(Calendar.DAY_OF_MONTH)).
                isEqualTo(now.get(Calendar.DAY_OF_MONTH));
    }
    // end::seedDatabase[]

    @Test
    @DataSet(value = "datasets/yml/users.yml")
    public void shouldLoadUserFollowers() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/json/users.json")
    public void shouldLoadUsersFromJsonDataset() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/xml/users.xml")
    public void shouldLoadUsersFromXmlDataset() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/csv/USER.csv", cleanBefore = true)
    public void shouldSeedDatabaseWithCSVDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join u.tweets t where t.content = 'dbunit rules!'").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
    }

    @Test
    @DataSet("xls/users.xls")
    public void shouldSeedDatabaseWithXLSDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join u.tweets t where t.content = 'dbunit rules!'").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
    }


    private static Connection connection;

    //called before each test by DBUnitRule
    private java.sql.Connection JDBCConnection() {
        try {
            //trigger db creation via JPA and clear jpa cache
            EntityManagerProvider.instance("rider-it").getEm().clear();

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:hsqldb:mem:rider;DB_CLOSE_DELAY=-1", "sa", "");
            }
            return connection; //we can reuse same connection as dbunit will control database state for us
        } catch (Exception e) {
            throw new RuntimeException("Could not aquire JDBC connection", e);
        }
    }

    @AfterClass//optional because connection will be closed at jvm exit
    public static void close() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                //OR DataSetExecutorImpl.getExecutorById(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID).getConnection().close();
            }
        } catch (SQLException e) {
            //
        }
    }


}
