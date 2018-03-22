package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.model.User;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


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

    @Test(expected = RuntimeException.class)
    @DataSet(value = "datasets/yml/user.yml", cleanBefore = true)
    public void shouldFindUser() {
        em().find(User.class, 1L);//adds user to the cache (reproducer for issue https://github.com/database-rider/database-rider/issues/74)
        throw new RuntimeException();
    }
    

    @Test
    @DataSet(value = "datasets/yml/empty.yml", cleanBefore = true)
    public void shouldNotFindUser() {
         User user = em().find(User.class, 1L);
         assertThat(user).isNull();
    }
    
     
}
