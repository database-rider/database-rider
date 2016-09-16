package com.github.database.rider.cdi;

import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.api.dataset.ExpectedDataSet;
import com.github.database.rider.model.User;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Created by rafael-pestano on 16/06/2016.
 */
// tag::expectedCDIDeclaration[]
@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class ExpectedDataSetCDIIt {

    @Inject
    EntityManager em;

// end::expectedCDIDeclaration[]

    // tag::expectedCDI[]
    @Test
    @DataSet(cleanBefore = true) //needed to activate interceptor (can be at class level)
    @ExpectedDataSet(value = "yml/expectedUsers.yml",ignoreCols = "id")
    public void shouldMatchExpectedDataSet() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        em.getTransaction().begin();
        em.persist(u);
        em.persist(u2);
        em.getTransaction().commit();
    }
    // end::expectedCDI[]
}
