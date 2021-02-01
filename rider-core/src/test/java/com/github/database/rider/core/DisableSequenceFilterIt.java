package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rmpestano on 23/07/15.
 */
@RunWith(JUnit4.class)
@DBUnit(disableSequenceFiltering = true)
public class DisableSequenceFilterIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it"); //<1>

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection()); //<2>

    @Test
    @DataSet(value = "datasets/yml/users.yml",
            tableOrdering = {"USER","TWEET","FOLLOWER"},
            executeStatementsBefore = {"DELETE FROM FOLLOWER","DELETE FROM TWEET","DELETE FROM USER"}//needed because other tests created user dataset
    )
    public void shouldSeedDataSetUsingTableCreationOrder() {
        List<User> users =  EntityManagerProvider.em().createQuery("select u from User u left join fetch u.tweets left join fetch u.followers").getResultList();
        assertThat(users).hasSize(2);
    }

}
