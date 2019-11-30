package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;

/**
 * Created by rmpestano on 6/21/16.
 */

@RunWith(JUnit4.class)
@DBUnit(cacheConnection = false)
public class TransactionIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.newInstance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("TransactionIt", emProvider.connection());

    @Test
    @DataSet(cleanBefore = true, executorId = "TransactionIt")
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldManageTransactionInsideTest() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
        EntityManagerProvider.tx().commit();
    }

    //tag::transaction[]
    @Test
    @DataSet(cleanBefore = true, transactional = true, executorId = "TransactionIt")
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    @DBUnit(cacheConnection = true)
    public void shouldManageTransactionAutomatically() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
    }
    //end::transaction[]


}
