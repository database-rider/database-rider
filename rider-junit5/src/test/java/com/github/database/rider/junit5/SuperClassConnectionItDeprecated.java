package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitPlatform.class)
public class SuperClassConnectionItDeprecated extends BaseDBTestDeprecated {

    @Test
    @DataSet(value = "usersWithTweet.yml")
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

}
