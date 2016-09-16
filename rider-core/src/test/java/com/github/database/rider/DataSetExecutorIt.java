package com.github.database.rider;

import static com.github.database.rider.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.database.rider.configuration.DataSetConfig;
import com.github.database.rider.connection.ConnectionHolderImpl;
import com.github.database.rider.dataset.DataSetExecutorImpl;
import com.github.database.rider.exception.DataBaseSeedingException;
import com.github.database.rider.util.EntityManagerProvider;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.model.Follower;
import com.github.database.rider.model.User;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(JUnit4.class)
public class DataSetExecutorIt {

    public EntityManagerProvider emProvider = EntityManagerProvider.instance("executor-it");

    private static DataSetExecutorImpl executor;

    @BeforeClass
    public static void setup() {
        executor = DataSetExecutorImpl.instance("executor-name", new ConnectionHolderImpl(EntityManagerProvider.instance("executor-it").connection()));
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        Connection connection = executor.getConnection();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void shouldSeedDataSetDisablingContraints() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").disableConstraints(true);
        executor.createDataSet(DataSetConfig);
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").executeStatementsAfter(new String[]{"SET DATABASE REFERENTIAL INTEGRITY FALSE;"});
        executor.createDataSet(DataSetConfig);
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }


    @Test
    public void shouldNotSeedDataSetWithoutSequenceFilter() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
            useSequenceFiltering(false).
            executeStatementsAfter(new String[] { "DELETE FROM User" });//needed because other tests creates users and as the dataset is not created in this test the CLEAN is not performed
        try {
            executor.createDataSet(DataSetConfig);
        }catch (DataBaseSeedingException e){
            assertThat(e.getMessage()).isEqualTo("Could not initialize dataset: datasets/yml/users.yml");
        }
    }

    @Test
    public void shouldSeedDataSetUsingTableCreationOrder() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
            tableOrdering(new String[]{"USER","TWEET","FOLLOWER"}).
            executeStatementsBefore(new String[]{"DELETE FROM FOLLOWER","DELETE FROM TWEET","DELETE FROM USER"}).//needed because other tests created user dataset
           useSequenceFiltering(false);
        DataSetExecutorImpl.instance(new ConnectionHolderImpl(emProvider.connection())).createDataSet(DataSetConfig);
        List<User> users =  EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2);
    }

    @Test
    public void shouldSeedUserDataSet() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
            useSequenceFiltering(true);
        executor.createDataSet(DataSetConfig);
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void shouldLoadUserFollowers() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml");
        executor.createDataSet(DataSetConfig);
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    public void shouldLoadUsersFromJsonDataset() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/json/users.json");
        executor.createDataSet(DataSetConfig);
        User user = (User) emProvider.clear().em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    public void shouldLoadUsersFromXmlDataset() {
        DataSetConfig DataSetConfig = new DataSetConfig("datasets/xml/users.xml");
        executor.createDataSet(DataSetConfig);
        User user = (User) emProvider.clear().em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }


    @Test
    public void shouldNotCreateDataSetWithoutConnection(){
        DataSetExecutorImpl executor = DataSetExecutorImpl.instance(new ConnectionHolderImpl(null));
        try{
            executor.createDataSet(new DataSetConfig("test-dataset"));
        }catch (DataBaseSeedingException e){
            assertThat(e.getMessage()).isEqualTo("Could not initialize dataset: test-dataset");
        }

    }

    @Test
    public void shouldNotCreateExecutiorWithoutConnection(){
        try{
            DataSetExecutorImpl executor = DataSetExecutorImpl.instance(null);
        }catch (RuntimeException e){
            assertThat(e.getMessage()).isEqualTo("Invalid connection");
        }

    }


}
