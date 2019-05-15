package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.schema.User;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 26/10/17.
 */
@RunWith(JUnitPlatform.class)
@DataSet(cleanBefore = true)
public class DBUnitJUnit5ChemaIt {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("schema-pu").clear().connection();


    @DBRider
    @DataSet(value = "usersWithTweetAndSchema.yml")
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @DBRider
    @DataSet("usersWithTweetAndSchema.yml") //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
    public void shouldUpdateUser() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        user.setName("@rmpestano");
        EntityManagerProvider.em().merge(user);
        User updatedUser = getUser(1L);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
    }

    @DBRider
    @DataSet(value = "usersWithTweetAndSchema.yml", transactional = true, cleanBefore = true)
    @ExpectedDataSet("expectedUser.yml")
    public void shouldDeleteUser() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        EntityManagerProvider.em().remove(user.getTweets().get(0));
        EntityManagerProvider.em().remove(user);
    }



    public User getUser(Long id){
        return (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = :id").
                setParameter("id", id).getSingleResult();
    }


}
