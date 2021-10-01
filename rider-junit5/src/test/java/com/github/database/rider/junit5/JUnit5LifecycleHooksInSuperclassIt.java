package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.Rider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Rider
@RunWith(JUnitPlatform.class)
public class JUnit5LifecycleHooksInSuperclassIt extends BaseLifecycleHooks {

    private static ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();


    @BeforeEach //the dataset from superclass will be inserted before and then we will append new entries in this class
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true, strategy = SeedStrategy.INSERT)
    public void loadDataSetBeforeEachOnTestClass() {
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet(value = "expectedUsersAndTweetsSuperclass.yml", orderBy = "CONTENT")
    public void shouldHaveUserAndTweetsInDB() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(2)
                .extracting("content")
                .contains("tweet before each!", "tweet before each on superclass!");
    }

}
