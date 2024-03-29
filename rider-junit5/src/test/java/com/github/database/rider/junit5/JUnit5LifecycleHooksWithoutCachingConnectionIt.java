package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.*;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@RunWith(JUnitPlatform.class)
@DBUnit(url = "jdbc:hsqldb:mem:junit5;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa", cacheConnection = false)
public class JUnit5LifecycleHooksWithoutCachingConnectionIt {


    @BeforeAll
    public static void loadDataSetBeforeAll() {
        EntityManagerProvider.instance("junit5-pu");//init db used
    }

    @BeforeEach
    @DataSet(value = "tweetBeforeEach.yml", disableConstraints = true)
    public void loadDataSetBeforeEach() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isEmpty();
        List<Tweet> tweets =  EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
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
        List<Tweet> tweets =  EntityManagerProvider.em().createQuery("select t from Tweet t").getResultList();
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
    @ExpectedDataSet(value = "usersAndTweetsAfterAll.yml")
    public static void loadDataSetAfterAll() {
    }

}
