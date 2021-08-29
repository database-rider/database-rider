package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


@RunWith(JUnit4.class)
@DataSet(value = "yml/empty.yml", cleanAfter = true)
@DBUnit(mergeDataSets = true, mergingStrategy = DataSetMergingStrategy.CLASS)
public class MergeDataSetClassLevelFirstIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @DataSet(value = "yml/user.yml", cleanAfter = false) //clean after is overridden by merging dataset
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);//2 because user.yaml dataset is loaded after empty.yml
    }

    @AfterClass
    public static void after(){
        List<User> users = em().createQuery("select u from User u").getResultList();
        if(users != null && !users.isEmpty()){
            fail("users should be empty");
        }
    }
}
