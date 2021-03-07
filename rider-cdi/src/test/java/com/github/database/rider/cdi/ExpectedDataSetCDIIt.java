package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.cdi.model.Tweet;
import com.github.database.rider.cdi.model.User;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.replacers.NullReplacer;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rafael-pestano on 16/06/2016.
 */
// tag::expectedCDIDeclaration[]
@RunWith(CdiTestRunner.class)
@DBUnitInterceptor
public class ExpectedDataSetCDIIt {

    @Inject
    EntityManager em;


    @Test
    @DataSet(cleanBefore = true) //needed to activate interceptor (can be at class level)
    @ExpectedDataSet(value = "yml/expectedUsers.yml", ignoreCols = "id")
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

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldMatchExpectedDataSetUsingRegex() {
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
    @DataSet(value = "yml/user.yml", disableConstraints = true)
    @ExpectedDataSet(value = "yml/expectedUser.yml", ignoreCols = "id")
    public void shouldMatchExpectedDataSetAfterSeedingDataBase() {
        em.getTransaction().begin();
        em.remove(em.find(User.class, 1L));
        em.getTransaction().commit();
    }

    @Test
    @DataSet(value = "yml/empty.yml", disableConstraints = true)
    @ExpectedDataSet(value = "yml/expectedUsersIgnoreOrder.yml", orderBy = "name")
    public void shouldMatchExpectedDataSetIgnoringRowOrder() {
        User u1 = new User();
        u1.setName("@arhohuttunen");
        User u2 = new User();
        u2.setName("@realpestano");
        User u3 = new User();
        u3.setName("@dbunit");
        em.getTransaction().begin();
        em.persist(u1);
        em.persist(u2);
        em.persist(u3);
        em.getTransaction().commit();
    }

    @Test
    @DataSet(value = "yml/empty.yml", disableConstraints = true)
    @ExpectedDataSet(value = "yml/expectedUsersAndTweetsIgnoreOrder.yml", orderBy = {"name", "content"})
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
        em.getTransaction().begin();
        em.persist(u1);
        em.persist(u2);
        em.persist(u3);
        em.persist(t3);
        em.persist(t2);
        em.persist(t1);
        em.getTransaction().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSet() {
        em.getTransaction().begin();
        em.remove(em.find(User.class, 1L));
        em.remove(em.find(User.class, 2L));
        em.getTransaction().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true, transactional = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSetWithTransaction() {
        em.remove(em.find(User.class, 1L));
        em.remove(em.find(User.class, 2L));
    }


    @Test
    @DataSet(cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = {"yml/user.yml", "yml/tweet.yml"}, ignoreCols = {"id", "user_id"})
    public void shouldMatchMultipleDataSets() {
        User u = new User();
        u.setName("@realpestano");
        User u2 = new User();
        u2.setName("@dbunit");
        em.persist(u);
        em.persist(u2);
        Tweet t = new Tweet();
        t.setLikes(10);
        t.setContent("dbunit rules again!");
        em.persist(t);
    }

    @Test
    @DataSet(value = "datasets/csv/USER.csv", cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "datasets/csv/expected/USER.csv")
    public void shouldMatchCsvDataSet() {
        User user = (User) em.createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        user.setName("@dbrider");
        assertThat(user.getTweets()).isNotNull().hasSize(1);
        user.getTweets().get(0).setContent("database rider rules!");
    }

    @Test
    @DataSet(value = "yml/user.yml", transactional = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsersContains.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContains() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em.persist(u);
    }

    @Test
    @DataSet(value = "yml/user.yml", transactional = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsersContainsColumns.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumns() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em.persist(u);
    }

    @Test
    @DataSet(value = {"yml/user.yml", "yml/empty.yml"}, transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContainsColumnsRegex.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumnsRegex() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em.persist(u);
    }

    @Test
    @DataSet(value = {"yml/user.yml", "yml/empty.yml"}, transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContains.yml", compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    public void shouldMatchExpectedDataSetContainsIgnoringColumn() {
        User u = new User();
        u.setId(new Random(System.currentTimeMillis()).nextInt());
        u.setName("@dbrider");
        em.persist(u);
    }

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "yml/null-replacements.yml", ignoreCols = "id", replacers= NullReplacer.class)
    public void shouldMatchExpectedDataSetNullReplaced() {
        User u = new User(1);
        Tweet t = new Tweet();
        t.setId("1");
        t.setContent(null);
        t.setUser(u);
        Tweet t2 = new Tweet();
        t2.setId("2");
        t2.setContent("null");
        t2.setUser(u);
        em.getTransaction().begin();
        em.persist(u);
        em.persist(t);
        em.persist(t2);
        em.getTransaction().commit();
    }
}
