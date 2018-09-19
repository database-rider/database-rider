package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import static com.github.database.rider.core.util.EntityManagerProvider.*;
import org.junit.AfterClass;

// tag::declaration[]
@RunWith(JUnit4.class)
@DataSet(value = "yml/tweet.yml", executeScriptsAfter = "addUser.sql", executeStatementsBefore = "INSERT INTO USER VALUES (8,'user8')")
@DBUnit(mergeDataSets = true) //<1>
public class MergeDataSetsIt {

// end::declaration[]
    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    
// tag::test-method[]    
    
    @Test
    @DataSet(value = "yml/user.yml", executeScriptsAfter = "tweets.sql", executeStatementsBefore = "INSERT INTO USER VALUES (9,'user9')", strategy = SeedStrategy.INSERT)
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(4);  //2 users from user.yml plus 1 from  class level 'executeStatementsBefore' and 1 user from method level 'executeStatementsBefore'

        User user = (User) em().createQuery("select u from User u where u.id = 9").getSingleResult();//statement before at test level
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(9);
        user = (User) em().createQuery("select u from User u where u.id = 1").getSingleResult();

        assertThat(user.getTweets()).isNotEmpty(); //tweets comes from class level annotation merged with method level
        assertThat(user.getTweets().get(0).getContent()).isEqualTo("dbunit rules again!");
    }
// end::test-method[]     

// tag::after-test[]       
    @AfterClass
    public static void afterTest() {
        User user = (User) em().createQuery("select u from User u where u.id = 10").getSingleResult();//scripts after from class level dataset
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(10);

        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = 10").getSingleResult();//scripts after on test level
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo("10");
    }

}
//end::after-test[]    
