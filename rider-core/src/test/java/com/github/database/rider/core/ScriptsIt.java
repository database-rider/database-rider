package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.After;

/**
 * Created by pestano on 27/02/16.
 */
@RunWith(JUnit4.class)
@DBUnit(cacheConnection = true)
public class ScriptsIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it",emProvider.connection());

    @BeforeClass
    public static void before() {
        EntityManagerProvider.tx("rules-it").begin();
        EntityManagerProvider.em().createNativeQuery("DELETE FROM USER").executeUpdate();
        EntityManagerProvider.em().createNativeQuery("INSERT INTO USER VALUES (6,'user6')").executeUpdate();
        EntityManagerProvider.em().flush();
        EntityManagerProvider.tx().commit();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(1);
    }

    @Test
    @DataSet(value = "yml/users.yml", executeScriptsBefore = {"users.sql","tweets.sql"}, executorId = "rules-it",
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
        return EntityManagerProvider.newInstance("rules-it").em().createQuery(sql).getResultList();
    }
    
    @After
    public void aTest() {
        System.out.println("");
    }

    @AfterClass
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
