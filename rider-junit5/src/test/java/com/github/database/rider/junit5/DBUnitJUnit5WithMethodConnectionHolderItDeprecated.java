package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.junit5.util.EntityManagerProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 28/08/16.
 */
@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class DBUnitJUnit5WithMethodConnectionHolderItDeprecated {

    //DBUnitExtension will get connection by reflection so either declare a field or a method with ConncetionHolder as return typr
    private ConnectionHolder getConnection() {
        return () -> instance("junit5-pu").connection();
    }

    @Test
    @DataSet(value = "usersWithTweet.yml")
    public void shouldListUsers() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DataSet(cleanBefore = true) //avoid conflict with other tests
    public void shouldInsertUser() {
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        tx().begin();
        em().persist(user);
        tx().commit();
        User insertedUser = (User) em().createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getId()).isNotNull();
    }

    @Test
    @DataSet("usersWithTweet.yml")
    //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
    public void shouldUpdateUser() {
        User user = (User) em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        //tx().begin(); 
        user.setName("@rmpestano");
        em().merge(user);
        //tx().commit(); //no needed because of first level cache 
        User updatedUser = getUser(1L);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
    }

    @Test
    @DataSet(value = "usersWithTweet.yml", transactional = true)
    @ExpectedDataSet("expectedUser.yml")
    public void shouldDeleteUser() {
        User user = (User) em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        em().remove(user.getTweets().get(0));
        em().remove(user);
    }


    public User getUser(Long id) {
        return (User) em().createQuery("select u from User u where u.id = :id").
                setParameter("id", id).getSingleResult();
    }
}
