package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(JUnit4.class)
@DataSet(value = "yml/empty.yml")
@DBUnit(mergeDataSets = true, mergingStrategy = DataSetMergingStrategy.METHOD)
public class MergeDataSetMethodLevelFirstIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @DataSet(value = "yml/user.yml")
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0); //zero because empty dataset is loaded after user.yml
    }
}
