package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.dataset.DataSet;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@DBUnitInterceptor
@RunWith(CdiTestRunner.class)
@DataSet(value = "yml/empty.yml")
@DBUnit(mergeDataSets = true, mergingStrategy = DataSetMergingStrategy.METHOD)
public class MergeDataSetsMethodLevelFirstCDIIt {

    @Inject
    EntityManager em;

    @Test
    @DataSet(value = "yml/users.yml")
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(0);//zero because empty.yml dataset is loaded after user.yml
    }

}
