package com.github.database.rider;

import com.github.database.rider.api.connection.ConnectionHolder;
import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.util.EntityManagerProvider;
import com.github.database.rider.model.Follower;
import com.github.database.rider.model.User;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(JUnit4.class)
public class ConnectionHolderIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("conn-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("ConnectionHolderIt",new ConnectionHolder() {
        @Override
        public Connection getConnection() {
            return initConnection();
        }
    });

    private Connection initConnection() {
        return emProvider.connection();
    }



    @Test
    @DataSet(value = "datasets/yml/users.yml", executorId = "ConnectionHolderIt")
    public void shouldLoadUserFollowers() {
        User user = (User) emProvider.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/json/users.json", executorId = "ConnectionHolderIt")
    public void shouldLoadUsersFromJsonDataset() {
        User user = (User) emProvider.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value = "datasets/xml/users.xml", executorId = "ConnectionHolderIt")
    public void shouldLoadUsersFromXmlDataset() {
        User user = (User) emProvider.em().createQuery("select u from User u left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

}
