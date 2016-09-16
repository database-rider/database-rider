package com.github.database.rider.junit5;

import com.github.database.rider.api.connection.ConnectionHolder;
import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by pestano on 26/02/16.
 */
@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class CleanBeforeAfterIt {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();


    @BeforeAll
    public static void before(){
        EntityManagerProvider.em("junit5-pu").getTransaction().begin();
        EntityManagerProvider.em().createNativeQuery("DELETE FROM USER").executeUpdate();
        EntityManagerProvider.em().createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        EntityManagerProvider.em().flush();
        EntityManagerProvider.em().getTransaction().commit();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);

    }

    @AfterAll
    public static void after(){
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        if(users != null && !users.isEmpty()){
            fail("users should be empty");
        }
    }


    @Test
    @DataSet(value = "users.yml", cleanBefore = true, cleanAfter = true)
    public void shouldCleanDatabaseBefore() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(2);//dataset has 2 users, user inserted in @Before must not be present
        User userInsertedInBefore = new User(6);//user inserted in @before has id 6
        assertThat(users).doesNotContain(userInsertedInBefore);
    }
}
