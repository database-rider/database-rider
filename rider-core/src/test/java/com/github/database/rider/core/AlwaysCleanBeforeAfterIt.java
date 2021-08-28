package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by pestano on 26/02/16.
 */
@RunWith(JUnit4.class)
public class AlwaysCleanBeforeAfterIt {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(EntityManagerProvider.instance("rules-it").connection());


    @BeforeClass
    public static void before(){
        EntityManager em = EntityManagerProvider.instance("rules-it").em();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM USER").executeUpdate();
        em.createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        em.flush();
        em.getTransaction().commit();
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);

    }

    @AfterClass
    public static void after(){
        List<User> users = EntityManagerProvider.instance("rules-it").em().createQuery("select u from User u").getResultList();
        if(users != null && !users.isEmpty()){
            fail("users should be empty");
        }
    }


    @Test
    @DataSet(value = "datasets/yml/users.yml")
    @DBUnit(alwaysCleanAfter = true, alwaysCleanBefore = true)
    public void shouldCleanDatabaseBefore() {
        List<User> users = EntityManagerProvider.instance("rules-it").em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(2);//dataset has 2 users, user inserted in @Before must not be present
        User userInsertedInBefore = new User(6);//user inserted in @before has id 6
        assertThat(users).doesNotContain(userInsertedInBefore);
    }

}
