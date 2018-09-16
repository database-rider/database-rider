package com.github.database.rider.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.Tweet;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;

@DBUnitInterceptor
@DBUnit(mergeDataSets = true)
@RunWith(CdiTestRunner.class)
@DataSet(value = "yml/tweet.yml", executeScriptsAfter = "addUser.sql", executeStatementsBefore = "INSERT INTO USER VALUES (8,'user8')")
public class MergeDataSetsCDIIt {

    @Inject
    EntityManager em;

    @Test
    @DataSet(value = "yml/usersWithoutTweets.yml", disableConstraints=true, executeScriptsAfter = "tweets.sql", executeStatementsBefore = "INSERT INTO USER VALUES (9,'user9')", strategy = SeedStrategy.INSERT)
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em.createQuery("select u from User u").getResultList(); //2 users from user.yml plus 1 from  class level 'executeStatementsBefore' and 1 user from method level 'executeStatementsBefore'
        assertThat(users).isNotNull().isNotEmpty().hasSize(4);

        User user = (User) em.createQuery("select u from User u where u.id = 9").getSingleResult();//statement before
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(9);
        user = (User) em.createQuery("select u from User u where u.id = 1").getSingleResult();

        assertThat(user.getTweets()).isNotEmpty(); //tweets comes from class level annotation merged with method level
        assertThat(user.getTweets().get(0).getContent()).isEqualTo("dbunit rules again!");
    }

    @After
    public void afterTest() {
    	em.createQuery("select t from Tweet t").getResultList();
        User user = (User) em.createQuery("select u from User u where u.id = 10").getSingleResult();//scripts after
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(10);

        Tweet tweet = (Tweet) em.createQuery("select t from Tweet t where t.id = 10").getSingleResult();//scripts after
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo("10");
    }

}
