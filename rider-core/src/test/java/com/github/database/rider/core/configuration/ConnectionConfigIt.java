package com.github.database.rider.core.configuration;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.model.User;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rafael-pestano on 13/09/2016.
 */
@RunWith(JUnit4.class)
@DBUnit(url = "jdbc:hsqldb:mem:test;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa")
public class ConnectionConfigIt {

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance();



    @BeforeClass
    public static void initDB(){
        //trigger db creation
        EntityManagerProvider.instance("rules-it");
    }

    @Test
    @DataSet(value = "datasets/yml/user.yml")
    public void shouldSeedFromDeclaredConnection() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }
}
