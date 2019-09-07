package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by pestano on 26/02/16.
 */
@RunWith(JUnit4.class)
public class CleanSpecifiedTablesAfterIt {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(EntityManagerProvider.instance("rules-it").connection());


    @AfterClass
    public static void after(){
        List<User> users = em("rules-it").createQuery("select u from User u").getResultList();
        if(users != null && users.isEmpty()){
            fail("users should NOT be empty because we specified only 'FOLLOWER' table to be cleaned");
        }

        List<Tweet> tweets = em("rules-it").createQuery("select t from Tweet t").getResultList();
        if(tweets != null && tweets.isEmpty()){
            fail("tweets should NOT be empty because we specified only 'FOLLOWER' table to be cleaned");
        }

        List<Follower> followers = em("rules-it").createQuery("select f from Follower f").getResultList();
        if(followers != null && !followers.isEmpty()){
            fail("follower table should be empty as we specified only 'FOLLOWER' table to be cleaned");
        }
    }


    @Test
    @DataSet(value = "datasets/yml/users.yml", cleanAfter = true, skipCleaningFor = {"USER", "TWEET"})
    public void shouldCleanOnlySpecifiedTable() {
        List<User> users = em("rules-it").createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(2);

        List<Tweet> tweets = em("rules-it").createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull().hasSize(1);

        List<Follower> followers = em("rules-it").createQuery("select f from Follower f").getResultList();
        assertThat(followers).isNotNull().hasSize(1);
    }
}
