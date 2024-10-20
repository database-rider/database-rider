package com.github.database.rider.junit5;

import java.util.List;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.replacers.Replacer;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.dbunit.dataset.ReplacementDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@RunWith(JUnitPlatform.class)
public class JUnit5LifecycleHooksInSuperclassIt extends BaseLifecycleHooks {

    private static ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();

    /**
     * Custom timestamp replacer for test purpose only to ensure the order of insertions can be validated.
     */
    public static class UnixTimestampReplacer implements Replacer {
        @Override
        public void addReplacements(ReplacementDataSet dataSet) {
            long timestamp = System.currentTimeMillis();
            dataSet.addReplacementObject("[UNIX_TIMESTAMP]", timestamp);
        }
    }


    @BeforeEach //the dataset from superclass will be inserted before and then we will append new entries in this class
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true, strategy = SeedStrategy.INSERT, replacers = UnixTimestampReplacer.class)
    public void loadDataSetBeforeEachOnTestClass() {}

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet(value = "expectedUsersAndTweetsSuperclass.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldHaveUserAndTweetsInDB() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t order by t.date").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(5)
                .extracting("content")
            .contains(
                "parent super class tweet before all!",
                "super class tweet before all!",
                "parent super class tweet before each on superclass!",
                "super class tweet before each on superclass!",
                "tweet before each!");
    }

}
