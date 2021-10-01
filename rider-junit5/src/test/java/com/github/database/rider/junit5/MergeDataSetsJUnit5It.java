package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.Rider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(mergeDataSets = true)
@RunWith(JUnitPlatform.class)
@DataSet(value = "tweet.yml", executeScriptsAfter = "addUser.sql", executeStatementsBefore = "INSERT INTO USER VALUES (8,'user8')")
public class MergeDataSetsJUnit5It {

    private ConnectionHolder connectionHolder = ()
            -> EntityManagerProvider.instance("junit5-pu").connection();

    @Rider
    @DataSet(value = "users.yml", executeScriptsAfter = "tweets.sql", cleanBefore = true,
            executeStatementsBefore = "INSERT INTO USER VALUES (9,'user9')", strategy = SeedStrategy.INSERT)
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList(); //2 users from user.yml plus 1 from  class level 'executeStatementsBefore' and 1 user from method level 'executeStatementsBefore'
        assertThat(users).isNotNull().isNotEmpty().hasSize(4);

        User user = (User) em().createQuery("select u from User u where u.id = 9").getSingleResult();//statement before
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(9);
        user = (User) em().createQuery("select u from User u where u.id = 1").getSingleResult();

        assertThat(user.getTweets()).isNotEmpty(); //tweets comes from class level annotation merged with method level
        assertThat(user.getTweets().get(0).getContent()).isEqualTo("dbunit rules again!");
    }

    @AfterEach
    public void afterTest() {
        User user = (User) em().createQuery("select u from User u where u.id = 10").getSingleResult();//scripts after
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(10);

        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '10'").getSingleResult();//scripts after
        assertThat(tweet).isNotNull();
        assertThat(tweet.getId()).isEqualTo("10");
    }

}
