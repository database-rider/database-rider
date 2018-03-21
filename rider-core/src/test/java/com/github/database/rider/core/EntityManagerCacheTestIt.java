package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rmpestano on 21/03/18.
 */
@RunWith(JUnit4.class)
public class EntityManagerCacheTestIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @DataSet(value = "datasets/yml/user.yml", cleanBefore = true)
    public void shouldFindUser() {
        User user = em().find(User.class, 1L);
        assertThat(user).
                isNotNull().extracting("name")
                .contains("@realpestano");
                //.contains("ERROR"); //forcing an error here makes both tests fail (because of issue #74 here: https://github.com/database-rider/database-rider/issues/74#issuecomment-374573158)
    }
    

    @Test
    @DataSet(value = "datasets/yml/empty.yml", cleanBefore = true)
    public void shouldNotFindUser() {
         User user = em().find(User.class, 1L);
         assertThat(user).isNull();
    }
    
     
}
