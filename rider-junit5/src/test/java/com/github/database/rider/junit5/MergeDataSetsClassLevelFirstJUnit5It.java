package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.junit5.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@DataSet(value = "empty.yml")
@DBUnit(mergeDataSets = true, mergingStrategy = DataSetMergingStrategy.CLASS)
@RunWith(JUnitPlatform.class)
public class MergeDataSetsClassLevelFirstJUnit5It {

    private ConnectionHolder connectionHolder = ()
            -> EntityManagerProvider.instance("junit5-pu").connection();

    @DBRider
    @DataSet(value = "users.yml")
    public void shouldMergeDataSetsFromClassAndMethod() {
        List<User> users = em().createQuery("select u from User u").getResultList();
		assertThat(users).isNotNull().isNotEmpty().hasSize(2);//2 because user.yml dataset is loaded after empty.yml
    }

}
