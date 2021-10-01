package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRiderExtension;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DBRiderExtension.class) //<1>
@RunWith(JUnitPlatform.class) //<2>
@DataSet(cleanBefore = true)
public class DBUnitJUnit5NestedIt {

    private ConnectionHolder connectionHolder = () -> //<3>
            EntityManagerProvider.instance("junit5-pu").clear().connection();//<4>


    @Test
    @DataSet(value = "usersWithTweet.yml")
    public void shouldListUsers() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Nested
    class NestedTest {

        private ConnectionHolder connectionHolder = () -> //<3>
                EntityManagerProvider.instance("junit5-pu").clear().connection();//<4>

        @Test
        @DataSet(cleanBefore = true)
        public void shouldInsertUser() {
            User user = new User();
            user.setName("user");
            user.setName("@rmpestano");
            EntityManagerProvider.tx().begin();
            EntityManagerProvider.em().persist(user);
            EntityManagerProvider.tx().commit();
            User insertedUser = (User) EntityManagerProvider.em().createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
            assertThat(insertedUser).isNotNull();
            assertThat(insertedUser.getId()).isNotNull();
        }
    }

    @Nested
    class NestedExpectedDataSetTest {

        private ConnectionHolder connectionHolder = () -> //<3>
                EntityManagerProvider.instance("junit5-pu").clear().connection();//<4>

        @Test
        @DataSet(value = "usersWithTweet.yml", transactional = true, cleanBefore = true)
        @ExpectedDataSet(value = "expectedUser.yml", ignoreCols = "id")
        public void shouldDeleteUser() {
            User user = (User) EntityManagerProvider.em().createQuery("select u from User u  where u.id = 1").getSingleResult();
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("@realpestano");
            EntityManagerProvider.em().remove(user.getTweets().get(0));
            EntityManagerProvider.em().remove(user);
        }
    }

    @Test
    @DataSet(value = "usersWithTweet.yml",
            useSequenceFiltering = false,
            tableOrdering = {"USER", "TWEET"},
            executeStatementsBefore = {"DELETE FROM TWEET", "DELETE FROM USER"}
    )
    public void shouldSeedDataSetUsingTableCreationOrder() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u left join fetch u.tweets").getResultList();
        assertThat(users).hasSize(2);
    }
}
