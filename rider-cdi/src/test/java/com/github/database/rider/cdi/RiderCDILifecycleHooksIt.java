package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.Tweet;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class RiderCDILifecycleHooksIt {

    @Inject
    EntityManager em;
 
    @Before
    @DataSet(value = "yml/userAndTweetBefore.yml", disableConstraints = true)
    public void loadDataSetBeforeEach() {
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isEmpty();
        List<Tweet> tweets =  em.createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before each!");
    }

    @Test
    @DataSet("yml/usersWithoutTweets.yml")
    @ExpectedDataSet(value = "yml/expectedUsersAndTweets.yml", orderBy = "CONTENT")
    public void shouldHaveUserAndTweetsInDB() {
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
        List<Tweet> tweets =  em.createQuery("select t from Tweet t").getResultList();
        assertThat(tweets).isNotNull()
                .hasSize(1)
                .extracting("content")
                .contains("tweet before each!");
    }

    @After
    @ExpectedDataSet(value = "yml/expectedTweetsAfter.yml", orderBy = "CONTENT")
    public void verifyInvariantsAfterEach() {
    }

}
