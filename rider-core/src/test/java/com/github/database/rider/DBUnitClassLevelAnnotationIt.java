package com.github.database.rider;

import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.util.EntityManagerProvider;
import com.github.database.rider.api.configuration.DBUnit;
import com.github.database.rider.model.Follower;
import com.github.database.rider.model.User;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by pestano on 26/02/16.
 */
@RunWith(JUnit4.class)
@DataSet(value = "datasets/yml/users.yml")
@DBUnit(cacheConnection = true)
public class DBUnitClassLevelAnnotationIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


    @Test
    public void shouldLoadUserFollowersUsingClassLevelAnnotation() {
        User user = (User) emProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules!");
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2,1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    @DataSet(value="json/users_without_followers.json",cleanBefore = true)
    public void shouldOverrideClassLevelAnnotation() {
        User user = (User) emProvider.em().createQuery("select u from User u join fetch u.tweets left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals(user.getTweets().get(0).getContent(), "dbunit rules without followers!");
        assertThat(user.getFollowers()).isEmpty();
    }
}
