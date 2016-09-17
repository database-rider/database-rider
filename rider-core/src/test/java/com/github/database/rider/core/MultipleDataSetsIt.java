package com.github.database.rider.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(JUnit4.class)
public class MultipleDataSetsIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("executor1-pu");

    @Rule
    public EntityManagerProvider emProvider1 = EntityManagerProvider.instance("executor2-pu");

    @Rule
    public EntityManagerProvider emProvider2 = EntityManagerProvider.instance("executor3-pu");

    @Rule
    public DBUnitRule exec1Rule = DBUnitRule.instance("exec1",emProvider1.connection());

    @Rule
    public DBUnitRule exec2Rule = DBUnitRule.instance("exec2",emProvider2.connection());


    @Test
    @DataSet(value = "datasets/yml/users.yml",disableConstraints = true, executorId = "exec1")
    public void shouldSeedDataSetDisablingContraints() {
        User user = (User) emProvider1.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml",disableConstraints = true, executorId = "exec2")
    public void shouldSeedDataSetDisablingContraints2() {
        User user = (User) emProvider2.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", executeStatementsBefore = "SET DATABASE REFERENTIAL INTEGRITY FALSE;", executorId = "exec1")
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        User user = (User) emProvider1.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", executeStatementsBefore = "SET DATABASE REFERENTIAL INTEGRITY FALSE;", executorId = "exec2")
    public void shouldSeedDataSetDisablingContraintsViaStatement2() {
        User user = (User) emProvider2.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml",
        useSequenceFiltering = false,
        executorId = "exec1",
        tableOrdering = {"USER","TWEET","FOLLOWER"},
        executeStatementsBefore = {"DELETE FROM FOLLOWER","DELETE FROM TWEET","DELETE FROM USER"}//needed because other tests created user dataset
     )
     public void shouldSeedDataSetUsingTableCreationOrder() {
        List<User> users =  emProvider1.em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml",
        useSequenceFiltering = false,
        executorId = "exec2",
        tableOrdering = {"USER","TWEET","FOLLOWER"},
        executeStatementsBefore = {"DELETE FROM FOLLOWER","DELETE FROM TWEET","DELETE FROM USER"}//needed because other tests created user dataset
    )
    public void shouldSeedDataSetUsingTableCreationOrder2() {
        List<User> users =  emProvider2.em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", useSequenceFiltering = true, executorId = "exec1")
    public void shouldSeedUserDataSet() {
        User user = (User) emProvider1.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", useSequenceFiltering = true, executorId = "exec2")
    public void shouldSeedUserDataSet2() {
        User user = (User) emProvider2.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }


    @Test
    @DataSet(value = "datasets/yml/users.yml", executorId = "exec1")
    public void shouldLoadUserFollowers() {
        User user = (User) emProvider1.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", executorId = "exec2")
    public void shouldLoadUserFollowers2() {
        User user = (User) emProvider2.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }



    @Test
    @DataSet(value = "datasets/json/users.json", executorId = "exec1")
    public void shouldLoadUsersFromJsonDataset() {
        User user = (User) emProvider1.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/json/users.json", executorId = "exec2")
    public void shouldLoadUsersFromJsonDataset2() {
        User user = (User) emProvider2.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/xml/users.xml", executorId = "exec1")
    public void shouldLoadUsersFromXmlDataset() {
        User user = (User) emProvider1.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/xml/users.xml", executorId = "exec2")
    public void shouldLoadUsersFromXmlDataset2() {
        User user = (User) emProvider2.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }


    @AfterClass//optional
    public static void close() throws SQLException {
        DataSetExecutorImpl.getExecutorById("exec1").getConnection().close();
        DataSetExecutorImpl.getExecutorById("exec2").getConnection().close();
    }

}
