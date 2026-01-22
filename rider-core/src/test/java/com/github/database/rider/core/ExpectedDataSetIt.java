package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.util.EntityManagerProvider;
import junit.framework.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rmpestano on 6/15/16.
 */
// tag::expectedDeclaration[]
@RunWith(JUnit4.class)
public class ExpectedDataSetIt {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


// end::expectedDeclaration[]

    // tag::expected[]
    @Test
    @DataSet(cleanBefore = true)//<1>
    @ExpectedDataSet(value = "yml/expectedUsers.yml", ignoreCols = "id")
    public void shouldMatchExpectedDataSet() {
        EntityManagerProvider instance = EntityManagerProvider.newInstance("rules-it");
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        instance.tx().begin();
        instance.em().persist(u);
        instance.em().persist(u2);
        instance.tx().commit();
    }
    // end::expected[]


    // tag::faillingExpected[]
    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsers.yml", ignoreCols = "id")
    public void shouldNotMatchExpectedDataSet() {
        exceptionRule.expect(ComparisonFailure.class);
        exceptionRule.expectMessage("value (table=USER, row=0, col=NAME) expected:<[]expected user1> but was:<[non ]expected user1>");
        User u = new User();
        u.setName("non expected user1");
        User u2 = new User();
        u2.setName("non expected user2");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
        EntityManagerProvider.tx().commit();
    }
    // end::faillingExpected[]

    // tag::expectedRegex[]
    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldMatchExpectedDataSetUsingRegex() {
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
        EntityManagerProvider.tx().commit();
    }
    // end::expectedRegex[]

    // tag::expectedWithSeeding[]
    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true)
    @ExpectedDataSet(value = "yml/expectedUser.yml", ignoreCols = "id")
    public void shouldMatchExpectedDataSetAfterSeedingDataBase() {
        tx().begin();
        em().remove(EntityManagerProvider.em().find(User.class, 1L));
        tx().commit();
    }
    // end::expectedWithSeeding[]

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
        tx().begin();
        em().persist(u1);
        em().persist(u2);
        em().persist(u3);
        tx().commit();
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
        tx().begin();
        em().persist(u1);
        em().persist(u2);
        em().persist(u3);
        em().persist(t3);
        em().persist(t2);
        em().persist(t1);
        tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSet() {
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class, 1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class, 2L));
        EntityManagerProvider.tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true, transactional = true, cleanBefore = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSetWithTransaction() {
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class, 1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class, 2L));
    }


    @Test
    @DataSet(cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = {"yml/user.yml", "yml/tweet.yml"}, ignoreCols = {"id", "user_id"})
    public void shouldMatchMultipleDataSets() {
        User u = new User();
        u.setName("@realpestano");
        User u2 = new User();
        u2.setName("@dbunit");
        em().persist(u);
        em().persist(u2);

        Tweet t = new Tweet();
        t.setContent("dbunit rules again!");
        em().persist(t);
    }


    @Test
    @DataSet(value = "datasets/csv/USER.csv", cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "datasets/csv/expected/USER.csv")
    public void shouldMatchCsvDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
        user.setName("@dbrider");
        assertThat(user.getTweets()).isNotNull().hasSize(1);
        user.getTweets().get(0).setContent("database rider rules!");
    }

    @Test
    @DataSet(value = "yml/user.yml", transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContains.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContains() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = "yml/user.yml", transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContainsColumns.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumns() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = {"yml/user.yml","yml/empty.yml"}, transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContainsColumnsRegex.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContainsColumnsRegex() {
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(value = {"yml/user.yml","yml/empty.yml"}, transactional = true)
    @ExpectedDataSet(value = "yml/expectedUsersContains.yml", compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    public void shouldMatchExpectedDataSetContainsIgnoringColumn() {
        User u = new User();
        u.setId(new Random(System.currentTimeMillis()).nextInt());
        u.setName("@dbrider");
        em().persist(u);
    }

    @Test
    @DataSet(cleanBefore = true)//<1>
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
        tx().begin();
        em().persist(u);
        em().persist(t);
        em().persist(t2);
        tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet(value = "yml/expectedUserWithScripting.yml")
    public void shouldEvaluateScriptsInExpectedDataSet() {

    }

    @Test
    @DataSet(value = "yml/empty.yml", transactional = true)
    @ExpectedDataSet(value = "yml/expectedUserWithScripting.yml")
    public void shouldShowAssertionErrorWhenScriptsEvaluateToFalseInExpectedDataSet() {
        exceptionRule.expect(ComparisonFailure.class);
        exceptionRule.expectMessage("value (table=USER, row=0, col=NAME) expected:<[js:(value != null && value.contains('@realpestano'))]> but was:<[test1]>");
        User u = new User(1);
        u.setName("test1");
        User u2 = new User(2);
        u2.setName("test2");
        em().persist(u);
        em().persist(u2);
    }


    @Test
    @DataSet(value = "yml/users.yml", transactional = true)
    @ExpectedDataSet(value = "yml/expectedUserWithProlog.yml", compareOperation = CompareOperation.PROLOG)
    public void shouldSupportPrologCompareOperation() {
    }

    @Test
    @DataSet(value = "yml/user.yml", cleanBefore = true)
    @ExpectedDataSet("yml/user.yml")
    public void shouldMatchInitialDataset() {
    }

    @Test
    @DataSet(value = "yml/user.yml", cleanBefore = true)
    @ExpectedDataSet("yml/reversedUser.yml")
    public void shouldMatchReversedDataset() {
    }

    @Test
    @DataSet(value = "yml/otherUser.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("yml/user.yml")
    public void shouldMatchUpdatedInitialDataset() {
        emProvider.tx().begin();
        User user = emProvider.getEm().find(User.class, 2L);
        user.setName("@dbunit");
        emProvider.getEm().merge(user);
        emProvider.tx().commit();
    }

    @Test
    @DataSet(value = "yml/otherUser.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("yml/reversedUser.yml")
    public void shouldMatchUpdatedReversedDataset() {
        emProvider.tx().begin();
        User user = emProvider.getEm().find(User.class, 2L);
        user.setName("@dbunit");
        emProvider.getEm().merge(user);
        emProvider.tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("yml/newUser.yml")
    public void shouldMatchInitialDatasetWithNewUser() {
        User user = new User(3);
        user.setName("@new");
        emProvider.tx().begin();
        emProvider.getEm().persist(user);
        emProvider.tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("yml/newReversedUser.yml")
    public void shouldMatchReversedDatasetWithNewUser() {
        User user = new User(3);
        user.setName("@new");
        emProvider.tx().begin();
        emProvider.getEm().persist(user);
        emProvider.tx().commit();
    }

}
