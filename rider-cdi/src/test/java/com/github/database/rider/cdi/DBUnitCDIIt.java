package com.github.database.rider.cdi;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.core.exception.DataBaseSeedingException;
import com.github.database.rider.cdi.model.Tweet;
import com.github.database.rider.cdi.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 23/07/15.
 */

// tag::cdi-declaration[]
@RunWith(CdiTestRunner.class) //<1>
@DBUnitInterceptor //<2>
@DataSet(value = "yml/users.yml")
public class DBUnitCDIIt {
// end::cdi-declaration[]

    @Inject
    EntityManager em;

    @Test
    @DataSet(value = "",cleanBefore = true)
    public void shouldSeedDBWhenUsingEmptyDataSet() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    @Test(expected = DataBaseSeedingException.class)
    @DataSet(value="ymlzzz/users.yml",cleanBefore = true)
    public void shouldFailToSeedInexistentYMLDataSet() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    @Test(expected = DataBaseSeedingException.class)
    @DataSet(value = "jsonzzz/users.json",cleanBefore = true)
    public void shouldFailToSeedInexistentJSONDataSet() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    @Test(expected = DataBaseSeedingException.class)
    @DataSet(value="zzz/users.xml",cleanBefore = true)
    public void shouldFailToSeedInexistentXMLDataSet() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    @Test(expected = DataBaseSeedingException.class)
    @DataSet(value="users",cleanBefore = true)
    public void shouldFailToSeedDataSetWithoutExtension() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    @Test(expected = DataBaseSeedingException.class)
    @DataSet(value = "users.doc",cleanBefore = true)
    public void shouldNotFailToSeedUnknownDataSetFormat() {
        List<User> users = (List<User>) em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);
    }

    // tag::seedDatabase[]
    @Test
    @DataSet("yml/users.yml")
    public void shouldSeedUserDataSetUsingCdiInterceptor() {
        List<User> users = em.createQuery("select u from User u order by u.id asc").getResultList();
        User user1 = new User(1);
        User user2 = new User(2);
        Tweet tweetUser1 = new Tweet();
        tweetUser1.setId("abcdef12345");
        assertThat(users).isNotNull().hasSize(2).contains(user1, user2);
        List<Tweet> tweetsUser1 = users.get(0).getTweets();
        assertThat(tweetsUser1).isNotNull().hasSize(1).contains(tweetUser1);
    }
    // end::seedDatabase[]

    @Test
    @DataSet("json/users.json")
    public void shouldSeedUserDataSetUsingCdiInterceptorUsingJsonDataSet() {
        User user = (User) em.createQuery("select u from User u join fetch u.tweets join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        assertThat(user.getFollowers()).hasSize(1);
    }

    @Test
    @DataSet("xml/users.xml")
    public void shouldSeedUserDataSetUsingCdiInterceptorUsingXmlDataSet() {
        User user = (User) em.createQuery("select u from User u join fetch u.tweets join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        assertThat(user.getFollowers()).hasSize(1);
    }

    @Test
    @DataSet(value = "yml/users.yml", strategy = SeedStrategy.INSERT, cleanBefore = true,
            executeStatementsBefore = "INSERT INTO USER VALUES (3,'user3')"
    )
    public void shouldExecuteCommandBefore() {
        User user = (User) em.createQuery("select u from User u where u.id = 3").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(3);
        assertThat(user.getName()).isEqualTo("user3");
    }

    @Test
    public void shouldSeedDBUsingClassLevelDataSet() {
        List<User> users = em.createQuery("select u from User u order by u.id asc").getResultList();
        User user1 = new User(1);
        User user2 = new User(2);
        Tweet tweetUser1 = new Tweet();
        tweetUser1.setId("abcdef12345");
        assertThat(users).isNotNull().hasSize(2).contains(user1, user2);
        List<Tweet> tweetsUser1 = users.get(0).getTweets();
        assertThat(tweetsUser1).isNotNull().hasSize(1).contains(tweetUser1);
    }




    //TODO replacer test
    //TODO execute scripts after test
    //TODO disable constraints

}