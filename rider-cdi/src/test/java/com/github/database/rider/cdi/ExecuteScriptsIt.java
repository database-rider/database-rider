package com.github.database.rider.cdi;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by pestano on 27/02/16.
 */
@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class ExecuteScriptsIt {

    @Inject
    EntityManager em;

    @Before
    public void before() {
        em.getTransaction().begin();
        em.createNativeQuery("DELETE from FOLLOWER").executeUpdate();
        em.createNativeQuery("DELETE from TWEET").executeUpdate();
        em.createNativeQuery("DELETE from USER").executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        em.flush();
        em.getTransaction().commit();
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);
    }

    @Test
    @DataSet(value = "yml/users.yml", executeScriptsBefore = {"users.sql","tweets.sql"},
            executeScriptsAfter = "after.sql", strategy = SeedStrategy.INSERT)//NEED to be INSERT because clean will delete users inserted in script
    public void shouldExecuteScriptsBefore() {
        User userFromSqlScript = new User(10);
        List<User> users = listUsers("select u from User u where u.id = 6");
        assertThat(users).isNotNull().hasSize(0);//user insert in @Before was deleted by users.sql script
        users = listUsers("select u from User u");
        assertThat(users).isNotNull().hasSize(3)// two from users.yaml dataset and one from users.sql script
                .contains(userFromSqlScript);
    }

    private List<User> listUsers(String sql) {
        return em.createQuery(sql).getResultList();
    }

    @After
    public void after() {
        List<User> users = em.createQuery("select u from User u").getResultList();
        if (users == null || users.size() != 1) {
            fail("We should have 1 user after test execution");
        }
        User user = users.get(0);//after script deletes all users and insert one
        assertThat(user.getName()).isNotNull().isEqualTo("user-after");
    }

}
