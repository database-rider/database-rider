package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.List;

import static com.github.database.rider.junit5.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class AlwaysCleanBeforeAfterJunit5ItDeprecated {

    private ConnectionHolder connectionHolder = () -> EntityManagerProvider.instance("junit5-pu").clear().connection();


    @BeforeAll
    public static void before() {
        EntityManager em = em("junit5-pu");
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM USER").executeUpdate();
        em.createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        em.flush();
        em.getTransaction().commit();
        List<User> users = em.createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);
    }

    @Test
    @DataSet(value = "users.yml")
    @DBUnit(alwaysCleanAfter = true, alwaysCleanBefore = true)
    public void shouldCleanDatabaseBefore() {
        List<User> users = em("junit5-pu").createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(2);//dataset has 2 users, user inserted in @Before must not be present
        User userInsertedInBefore = new User(6);//user inserted in @before has id 6
        assertThat(users).doesNotContain(userInsertedInBefore);
    }

    @AfterEach
    public void afterEach() {
        after();
    }

    @AfterAll
    public static void after() {
        List<User> users = em("junit5-pu").createQuery("select u from User u").getResultList();
        if (users != null && !users.isEmpty()) {
            fail("users should be empty");
        }
    }

}
