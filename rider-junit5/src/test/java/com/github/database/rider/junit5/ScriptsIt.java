package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRiderExtension;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by pestano on 27/02/16.
 */
@ExtendWith(DBRiderExtension.class)
@RunWith(JUnitPlatform.class)
public class ScriptsIt {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").clear().connection();

    @BeforeAll
    public static void before() {
        EntityManagerProvider.tx("junit5-pu").begin();
        EntityManagerProvider.em().createNativeQuery("DELETE FROM TWEET").executeUpdate();
        EntityManagerProvider.em().createNativeQuery("DELETE FROM USER").executeUpdate();
        EntityManagerProvider.em().createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        EntityManagerProvider.em().flush();
        EntityManagerProvider.tx().commit();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);
    }

    @Test
    @DataSet(value = "users.yml", executeScriptsBefore = "users.sql",
            executeScriptsAfter = "after.sql", strategy = SeedStrategy.INSERT)
//NEED to be INSERT because clean will delete users inserted in script
    public void shouldExecuteScriptsBefore() {
        User userFromSqlScript = new User(10);
        List<User> users = listUsers("select u from User u where u.id = 6");
        assertThat(users).isNotNull().hasSize(0);//user insert in @Before was deleted by users.sql script
        users = listUsers("select u from User u");
        assertThat(users).isNotNull().hasSize(3)// two from users.yaml dataset and one from users.sql script
                .contains(userFromSqlScript);
    }

    private List<User> listUsers(String sql) {
        return EntityManagerProvider.instance("junit5-pu").em().createQuery(sql).getResultList();
    }

    @AfterAll
    public static void after() throws InterruptedException {
        EntityManagerProvider.em().clear();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        if (users == null || users.size() != 1) {
            fail("We should have 1 user after test execution");
        }
        User user = users.get(0);//after script deletes all users and insert one
        assertThat(user.getName()).isNotNull().isEqualTo("user-after");
    }

}
