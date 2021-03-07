package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 28/08/16.
 */
//tag::declaration[]
@ExtendWith(DBUnitExtension.class) //<1>
@RunWith(JUnitPlatform.class) //<2>
@DataSet(cleanBefore = true)
public class DBUnitJUnit5It {

//end::declaration[]

//DBUnitExtension will get connection by reflection so either declare a field or a method with ConncetionHolder as return type
//tag::connectionField[]
    private ConnectionHolder connectionHolder = () -> //<3>
            EntityManagerProvider.instance("junit5-pu").clear().connection();//<4>

//end::connectionField[]

 

//tag::test[]
    @Test
    @DataSet(value = "usersWithTweet.yml")
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::test[]

    @Test
    @DataSet(cleanBefore = true) //avoid conflict with other tests
    public void shouldInsertUser() {
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(user);
        EntityManagerProvider.tx().commit();
        User insertedUser = (User) EntityManagerProvider.em().createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getId()).isNotNull();
    }

    @Test
    @DataSet("usersWithTweet.yml") //no need for clean before because DBUnit uses CLEAN_INSERT seeding strategy which clears involved tables before seeding
    public void shouldUpdateUser() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        //tx().begin(); 
        user.setName("@rmpestano");
        EntityManagerProvider.em().merge(user);
        //tx().commit(); //no needed because of first level cache 
        User updatedUser = getUser(1L);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("@rmpestano");
    }

    @Test
    @DataSet(value = "usersWithTweet.yml", transactional = true, cleanBefore = true)
    @ExpectedDataSet("expectedUser.yml")
    public void shouldDeleteUser() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        EntityManagerProvider.em().remove(user.getTweets().get(0));
        EntityManagerProvider.em().remove(user);
    }

    @Test
    public void shouldNotSeedDatabaseListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isEmpty();
    }


    public User getUser(Long id){
        return (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = :id").
                setParameter("id", id).getSingleResult();
    }

    @Test
    @DataSet(value = "usersWithTweet.yml", executeStatementsBefore = "SET DATABASE REFERENTIAL INTEGRITY FALSE;", executeStatementsAfter = "SET DATABASE REFERENTIAL INTEGRITY TRUE;")
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
    }



    @Test
    @DataSet(value = "usersWithTweet.yml",
            useSequenceFiltering = false,
            tableOrdering = {"USER","TWEET"},
            executeStatementsBefore = {"DELETE FROM TWEET","DELETE FROM USER"}
    )
    public void shouldSeedDataSetUsingTableCreationOrder() {
        List<User> users =  EntityManagerProvider.em().createQuery("select u from User u left join fetch u.tweets").getResultList();
        assertThat(users).hasSize(2);
    }
}
