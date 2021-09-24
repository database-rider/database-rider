package com.github.database.rider.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.User;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class SuperClassConnectionIt extends BaseDBTest {
	
	    @Test
	    @DataSet(value = "usersWithTweet.yml")
	    public void shouldListUsers() {
	        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
	        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
	    }

}
