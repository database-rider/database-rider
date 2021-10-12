package com.github.database.rider.junit5.incubating;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRider;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@DBUnit(url = "jdbc:hsqldb:mem:junit5;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa", mergeDataSets = true)
@DataSet(value = "tweet.yml")
@RunWith(JUnitPlatform.class)
public class JUnit5LifecycleHooksMergeDataSetsIt {

    private static ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();

    @BeforeEach
    @DataSet(value = "users.yml", disableConstraints = true)
    public void shouldMergeDataSetsFromClassAndLifecycleMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList(); //users comes from method level annotation merged with method level
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);

        User user = (User) em().createQuery("select u from User u where u.id = 1").getSingleResult();

        assertThat(user.getTweets()).isNotEmpty(); //tweets comes from class level annotation merged with method level
        assertThat(user.getTweets().get(0).getContent()).isEqualTo("dbunit rules again!");
    }

    @Test
    public void dummyTest() {
    }

}