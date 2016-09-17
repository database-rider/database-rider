package com.github.database.rider.core.cdi;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.cdi.api.DBUnitInterceptor;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;


/**
 * Created by rmpestano on 6/21/16.
 */
@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class TransactionCDIIt {

    @Inject
    EntityManager em;

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldManageTransactionInsideTest() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        em.getTransaction().begin();
        em.persist(u);
        em.persist(u2);
        em.getTransaction().commit();
    }

    @Test
    @DataSet(cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldManageTransactionAutomatically() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        em.persist(u);
        em.persist(u2);
    }
}
