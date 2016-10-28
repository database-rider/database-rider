package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.User;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 07/09/16.
 */
//tag::junit5-annotation[]
@RunWith(JUnitPlatform.class)
public class DBRiderAnnotationIt {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();


    @DBRider //<1>
    @DataSet(value = "usersWithTweet.yml")
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
}
//end::junit5-annotation[]
