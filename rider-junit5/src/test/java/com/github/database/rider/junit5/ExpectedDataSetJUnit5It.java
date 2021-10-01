package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBUnitExtension;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
public class ExpectedDataSetJUnit5It {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").connection();


    @Test
    @DataSet(cleanBefore = true) //needed to activate interceptor (can be at class level)
    @ExpectedDataSet(value = "expectedUsers.yml", ignoreCols = "id")
    public void shouldMatchExpectedDataSet() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        em().getTransaction().begin();
        em().persist(u);
        em().persist(u2);
        em().getTransaction().commit();
    }

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "expectedUsersRegex.yml")
    public void shouldMatchExpectedDataSetUsingRegex() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        em().getTransaction().begin();
        em().persist(u);
        em().persist(u2);
        em().getTransaction().commit();
    }

    @Test
    @DataSet(value = "user.yml", disableConstraints = true)
    @ExpectedDataSet(value = "expectedUser.yml", ignoreCols = "id")
    public void shouldMatchExpectedDataSetAfterSeedingDataBase() {
        em().getTransaction().begin();
        em().remove(em().find(User.class, 1L));
        em().getTransaction().commit();
    }

    @Test
    @DataSet(value = "empty.yml", disableConstraints = true)
    @ExpectedDataSet(value = "expectedUsersIgnoreOrder.yml", orderBy = "name")
    public void shouldMatchExpectedDataSetIgnoringRowOrder() {
        User u1 = new User();
        u1.setName("@arhohuttunen");
        User u2 = new User();
        u2.setName("@realpestano");
        User u3 = new User();
        u3.setName("@dbunit");
        em().getTransaction().begin();
        em().persist(u1);
        em().persist(u2);
        em().persist(u3);
        em().getTransaction().commit();
    }

    @Test
    @DataSet(value = "empty.yml", disableConstraints = true)
    @ExpectedDataSet(value = "expectedUsersAndTweetsIgnoreOrder.yml", orderBy = {"name", "content"})
    public void shouldMatchExpectedDataSetIgnoringRowOrderInMultipleTables() {
        User u1 = new User();
        u1.setName("@arhohuttunen");
        User u2 = new User();
        u2.setName("@realpestano");
        User u3 = new User();
        u3.setName("@dbunit");

        Tweet t1 = new Tweet();
        t1.setContent("tweet1");

        Tweet t2 = new Tweet();
        t2.setContent("tweet2");

        Tweet t3 = new Tweet();
        t3.setContent("tweet3");
        em().getTransaction().begin();
        em().persist(u1);
        em().persist(u2);
        em().persist(u3);
        em().persist(t3);
        em().persist(t2);
        em().persist(t1);
        em().getTransaction().commit();
    }

    @Test
    @DataSet(value = "user.yml", disableConstraints = true, cleanBefore = true)
    @ExpectedDataSet(value = "empty.yml")
    public void shouldMatchEmptyYmlDataSet() {
        em().getTransaction().begin();
        em().remove(em().find(User.class, 1L));
        em().remove(em().find(User.class, 2L));
        em().getTransaction().commit();
    }

    @Test
    @DataSet(value = "user.yml", disableConstraints = true, transactional = true, cleanBefore = true)
    @ExpectedDataSet(value = "empty.yml")
    public void shouldMatchEmptyYmlDataSetWithTransaction() {
        em().remove(em().find(User.class, 1L));
        em().remove(em().find(User.class, 2L));
    }


    @Test
    @DataSet(cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = {"user.yml", "tweet.yml"}, ignoreCols = {"id", "user_id"})
    public void shouldMatchMultipleDataSets() {
        User u = new User();
        u.setName("@realpestano");
        User u2 = new User();
        u2.setName("@dbunit");
        em().persist(u);
        em().persist(u2);
        Tweet t = new Tweet();
        t.setLikes(10);
        t.setContent("dbunit rules again!");
        em().persist(t);
    }


    @Test
    @DataSet(value = "datasets/csv/USER.csv", cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "datasets/csv/expected/USER.csv")
    public void shouldMatchCsvDataSet() {
        User user = (User) em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        user.setName("@dbrider");
        assertThat(user.getTweets()).isNotNull().hasSize(1);
        user.getTweets().get(0).setContent("database rider rules!");
    }


    @Test
    @DataSet(value = "user.yml", transactional = true)
    @ExpectedDataSet(value = "expectedUsersContainsColumns.yml", ignoreCols = "id", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumns() {
        User u = new User();
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = {"user.yml", "empty.yml"}, transactional = true)
    @ExpectedDataSet(value = "expectedUsersContainsColumnsRegex.yml", ignoreCols = "id", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumnsRegex() {
        User u = new User();
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = "user.yml", transactional = true)
    @ExpectedDataSet(value = "expectedUsersContains.yml", ignoreCols = "id", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContains() {
        User u = new User();
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = "empty.yml", transactional = true)
    @ExpectedDataSet(value = "null-replacements.yml", ignoreCols = "id",
            replacers = NullReplacer.class)
    @Disabled("Fails randomly with 'ExpectedDataSetJUnit5It.shouldMatchExpectedDataSetNullReplacer value (table=TWEET, row=0, col=CONTENT) expected:<null> but was:<null>'")
    public void shouldMatchExpectedDataSetNullReplacer() {
        User u = new User();
        Tweet t = new Tweet();
        t.setContent(null);
        t.setUser(u);
        Tweet t2 = new Tweet();
        t2.setContent("null");
        t2.setUser(u);
        em().persist(u);
        em().persist(t);
        em().persist(t2);
    }
}
