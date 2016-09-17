package com.github.database.rider.core.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.core.junit5.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Created by rmpestano on 6/21/16.
 */

@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class TransactionIt {


    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "expectedUsersRegex.yml")
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
    @DataSet(cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "expectedUsersRegex.yml")
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
