package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.cdi.api.RiderPU;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 23/07/15.
 */

@RunWith(CdiTestRunner.class)
@DBRider
public class MultipleEntityManagerIt {


    @Inject
    EntityManager em;

    @Inject
    @RiderPU("cdipu2")
    EntityManager em2;


    @Test
    @DataSet("yml/users.yml")
    public void shouldListUsersFromDefaultEntityManager() {
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet("yml/users.yml")
    public void shouldListUsersFromEntityManager2() {
        List<User> users = em2.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DataSet(provider = UserDataSetProvider.class)
    public void shouldListUsersUsingDataSetProvider() {
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet(provider = UserDataSetProvider.class, cleanBefore = true)
    public void shouldListUsersFromCDIPU2UsingDataSetProvider() {
        List<User> users = em2.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    @DataSet(cleanBefore = true) //avoid conflict with other tests
    public void shouldInsertUser() {
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        User insertedUser = (User) em.createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getId()).isNotNull();
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet(cleanBefore = true) //avoid conflict with other tests
    public void shouldInsertUserInCDI2PU() {
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        em2.getTransaction().begin();
        em2.persist(user);
        em2.getTransaction().commit();
        User insertedUser = (User) em2.createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getId()).isNotNull();
    }

    @Test
    @DataSet("yml/users.yml")
    //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
    public void shouldUpdateUser() {
        User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        //em.getTransaction().begin();
        user.setName("@rmpestano");
        em.merge(user);
        //em.getTransaction().commit(); //no needed because of first level cache
        User updatedUser = getUser(1, em);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet("yml/users.yml")
    //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
    public void shouldUpdateUserFromCDI2PU() {
        User user = (User) em2.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        //em.getTransaction().begin();
        user.setName("@rmpestano");
        em2.merge(user);
        //em.getTransaction().commit(); //no needed because of first level cache
        User updatedUser = getUser(1, em2);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
    }

    @Test
    @DataSet(value = "yml/users.yml", disableConstraints = true)
//disable constraints because User 1 has one tweet and a follower
    public void shouldDeleteUser() {
        User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();
        List<User> users = em.createQuery("select u from User u ").getResultList();
        assertThat(users).hasSize(1);
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet(value = "yml/users.yml", disableConstraints = true)
//disable constraints because User 1 has one tweet and a follower
    public void shouldDeleteUserFromCDI2PU() {
        User user = (User) em2.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        em2.getTransaction().begin();
        em2.remove(user);
        em2.getTransaction().commit();
        List<User> users = em2.createQuery("select u from User u ").getResultList();
        assertThat(users).hasSize(1);
    }

    @Test
    @DataSet(value = "yml/users.yml")
    public void shouldDeleteUserWithoutDisablingConstraints() {
        User user = (User) em.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        em.getTransaction().begin();
        em.createQuery("Delete from Tweet t where t.user.id = 1 ").executeUpdate();
        em.createQuery("Delete from Follower f where f.followedUser.id = 1 ").executeUpdate();
        em.remove(user);
        em.getTransaction().commit();
        List<User> users = em.createQuery("select u from User u ").getResultList();
        assertThat(users).hasSize(1);
    }

    @Test
    @DBRider(entityManagerName = "cdipu2")
    @DataSet(value = "yml/users.yml")
    public void shouldDeleteUserFromCDI2PUWithoutDisablingConstraints() {
        User user = (User) em2.createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        em2.getTransaction().begin();
        em2.createQuery("Delete from Tweet t where t.user.id = 1 ").executeUpdate();
        em2.createQuery("Delete from Follower f where f.followedUser.id = 1 ").executeUpdate();
        em2.remove(user);
        em2.getTransaction().commit();
        List<User> users = em2.createQuery("select u from User u ").getResultList();
        assertThat(users).hasSize(1);
    }


    public User getUser(Integer id, EntityManager em) {
        return (User) em.createQuery("select u from User u where u.id = :id").
                setParameter("id", id).getSingleResult();
    }

    public static class UserDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user")
                    .row()
                    .column("id", 1)
                    .column("name", "@dbunit")
                    .row()
                    .column("id", 2)
                    .column("name", "@dbrider");
            return builder.build();
        }
    }
}