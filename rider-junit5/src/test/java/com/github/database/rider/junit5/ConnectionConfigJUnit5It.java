package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rafael-pestano on 13/09/2016.
 */
@ExtendWith(DBUnitExtension.class) //<1>
@RunWith(JUnitPlatform.class) //<2>
@DBUnit(url = "jdbc:hsqldb:mem:junit5;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa")
public class ConnectionConfigJUnit5It {


    @BeforeAll
    public static void initDB(){
        //trigger db creation
        EntityManagerProvider.instance("junit5-pu");
    }

    @Test
    @DataSet(value = "users.yml", cleanBefore = true)
    public void shouldSeedFromDeclaredConnection() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }
}
