package com.github.database.rider.core;

import static com.github.database.rider.core.util.EntityManagerProvider.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.model.User;

import javax.persistence.EntityManager;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(JUnit4.class)
public class MultipleExecutorsIt {


    private static List<DataSetExecutorImpl> executors = new ArrayList<>(3);

    @BeforeClass
    public static void setup() {
        executors.add(DataSetExecutorImpl.instance("executor1", new ConnectionHolderImpl(instance("executor1-pu").connection())));
        executors.add(DataSetExecutorImpl.instance("executor2", new ConnectionHolderImpl(instance("executor2-pu").connection())));
        executors.add(DataSetExecutorImpl.instance("executor3", new ConnectionHolderImpl(instance("executor3-pu").connection())));
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        for (DataSetExecutorImpl executor : executors) {
            Connection connection = executor.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }

    }

    @Test
    public void shouldSeedDataSetDisablingContraints() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").disableConstraints(true);
            executor.createDataSet(DataSetConfig);
            User user = (User) instance(executor.getExecutorId() + "-pu").em().createQuery("select u from User u where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
        }

    }

    @Test
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").executeStatementsAfter(new String[]{"SET DATABASE REFERENTIAL INTEGRITY FALSE;"});
            executor.createDataSet(DataSetConfig);
            User user = (User) instance(executor.getExecutorId() + "-pu").em().createQuery("select u from User u where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
        }

    }


    @Test
    public void shouldNotSeedDataSetWithoutSequenceFilter() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
                useSequenceFiltering(false).
                executeStatementsAfter(new String[] { "DELETE FROM User" });//needed because other tests creates users and as the dataset is not created in this test the CLEAN is not performed
            try{
                executor.createDataSet(DataSetConfig);
            }catch (DataBaseSeedingException e){
                assertThat(e.getMessage()).isEqualTo("Could not initialize dataset: datasets/yml/users.yml");
            }
        }

    }

    @Test
    public void shouldSeedDataSetUsingTableCreationOrder() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
                tableOrdering(new String[]{"USER","TWEET","FOLLOWER"}).
                executeStatementsBefore(new String[]{"DELETE FROM FOLLOWER","DELETE FROM TWEET","DELETE FROM USER"}).//needed because other tests created user dataset
                useSequenceFiltering(false);
            executor.createDataSet(DataSetConfig);
            List<User> users =  instance(executor.getExecutorId() + "-pu").em().createQuery("select u from User u").getResultList();
            assertThat(users).hasSize(2);
        }

    }

    @Test
    public void shouldSeedUserDataSet() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml").
                useSequenceFiltering(true);
            executor.createDataSet(DataSetConfig);
            EntityManager em = instance(executor.getExecutorId() + "-pu").em();
            em.clear();//jpa cache
            User user = (User) em.createQuery("select u from User u where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
        }

    }

    @Test
    public void shouldLoadUserFollowers() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/yml/users.yml");
            executor.createDataSet(DataSetConfig);
            EntityManager em = instance(executor.getExecutorId() + "-pu").em();
            em.clear();//jpa cache
            User user = (User) em.createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
            assertThat(user.getTweets()).hasSize(1);
            Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
            assertThat(user.getFollowers()).isNotNull().hasSize(1);
            Follower expectedFollower = new Follower(2,1);
            assertThat(user.getFollowers()).contains(expectedFollower);
        }

    }

    @Test
    public void shouldLoadUsersFromJsonDataset() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/json/users.json");
            executor.createDataSet(DataSetConfig);
            EntityManager em = instance(executor.getExecutorId() + "-pu").em();
            em.clear();
            User user = (User) em.createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
            assertThat(user.getTweets()).hasSize(1);
            Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
            assertThat(user.getFollowers()).isNotNull().hasSize(1);
            Follower expectedFollower = new Follower(2,1);
            assertThat(user.getFollowers()).contains(expectedFollower);
        }

    }

    @Test
    public void shouldLoadUsersFromXmlDataset() {
        for (DataSetExecutorImpl executor : executors) {
            DataSetConfig DataSetConfig = new DataSetConfig("datasets/xml/users.xml");
            executor.createDataSet(DataSetConfig);
            EntityManager em = instance(executor.getExecutorId() + "-pu").em();
            em.clear();//jpa cache
            User user = (User) em.createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(1);
            assertThat(user.getTweets()).hasSize(1);
            Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
            assertThat(user.getFollowers()).isNotNull().hasSize(1);
            Follower expectedFollower = new Follower(2,1);
            assertThat(user.getFollowers()).contains(expectedFollower);
        }

    }

}
