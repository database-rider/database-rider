package com.github.database.rider.junit5.incubating;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.*;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@RunWith(JUnitPlatform.class)
public class JUnit5LifecycleHooksIt {

    private static ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();

    @BeforeAll
    @DataSet(value = "usersAndTweetsBeforeAll.yml", disableConstraints = true)
    static void loadDataSetBeforeAll() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isEmpty();
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before all!");
    }

    @BeforeEach
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true)
    void loadDataSetBeforeEach() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isEmpty();
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before each!")
                .doesNotContain("tweet before all!");//CLEAN_INSERT seed strategy removes this record
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet(value = "expectedUsersAndTweets.yml", orderBy = "CONTENT")
    public void shouldHaveUserAndTweetsInDB() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
        List<Tweet> tweets = EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before each!");
    }

    @AfterEach
    @ExpectedDataSet(value = "expectedTweetsAfterEach.yml", orderBy = "CONTENT")
    public void verifyInvariantsAfterEach() {
    }

    @AfterAll
    @DataSet(value = "usersAndTweetsAfterAll.yml", disableConstraints = true)
    static void loadDataSetAfterAll() {
    }

}
