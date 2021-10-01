package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRiderExtension;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 07/09/16.
 */
@ExtendWith(DBRiderExtension.class)
@RunWith(JUnitPlatform.class)
@DataSet("usersWithTweet.yml")
public class TestLevelIt {

    private ConnectionHolder connectionHolder = () -> //<3>
            EntityManagerProvider.instance("junit5-pu").connection();

    @Test
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
}
