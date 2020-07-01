package com.github.database.rider.core.dsl;

import com.github.database.rider.core.DataSetProviderIt;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.configuration.ExpectedDataSetConfig;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import junit.framework.ComparisonFailure;
import org.dbunit.DatabaseUnitException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.api.dataset.SeedStrategy.INSERT;
import static com.github.database.rider.core.api.dataset.SeedStrategy.TRUNCATE_INSERT;
import static com.github.database.rider.core.dsl.RiderDSL.DataSetConfigDSL.withDataSetConfig;
import static com.github.database.rider.core.dsl.RiderDSL.withConnection;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RiderDSLIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldCreateDataSetUsingDSL() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml"))
                .createDataSet();

        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2)
                .extracting("name")
                .contains("@realpestano", "@dbunit");

    }

    @Test
    public void shouldCreateMultipleDataSetsReusingDSLInstance() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml"))
                .createDataSet();

        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2)
                .extracting("name")
                .contains("@realpestano", "@dbunit");


        withConnection()
                .withDataSetConfig(new DataSetConfig("datasets/yml/empty.yml"))
                .createDataSet();

        users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isEmpty();


        //you also don't need to use connection dsl if it is already initialized
        RiderDSL.DataSetConfigDSL
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml"))
                .createDataSet();

        users = em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2)
                .extracting("name")
                .contains("@realpestano", "@dbunit");

        //you can also use static import if you're sure the connection is initialized

        withDataSetConfig(new DataSetConfig("datasets/yml/empty.yml"))
                .createDataSet();

        users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isEmpty();
    }

    @Test
  /*  @DataSet(provider = UserDataSetProvider.class,
            cleanBefore = true)*/
    public void shouldSeedDatabaseProgrammatically() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig().datasetProvider(DataSetProviderIt.UserDataSetProvider.class)
                        .cleanBefore(true))
                .createDataSet();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@dbunit", "@dbrider");
    }

    @Test
    //@DataSet(provider = UserDataSetProviderWithColumnsSyntax.class)
    public void shouldSeedDatabaseUsingDataSetProviderWithColumnsSyntax() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig().datasetProvider(DataSetProviderIt.UserDataSetProviderWithColumnsSyntax.class)
                        .cleanBefore(true))
                .createDataSet();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@dbunit", "@dbrider");
    }

    @Test
    /*same as: @DataSet(value = "yml/lowercaseUsers.yml") // note that hsqldb tables are in uppercase
     @DBUnit(caseSensitiveTableNames = false) */
    public void shouldListUsersWithCaseInSensitiveTableNames() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/lowercaseUsers.yml"))
                .withDBUnitConfig(new DBUnitConfig().addDBUnitProperty("caseSensitiveTableNames", false))
                .createDataSet();
        List<com.github.database.rider.core.model.lowercase.User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    //@DataSet(value = "datasets/yml/user.yml", cleanBefore = true)
    public void shouldSeedDatabase() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/user.yml")
                        .cleanBefore(true))
                .createDataSet();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().
                hasSize(2);
    }

    @Test
    //@DataSet(value = "datasets/yml/users.yml", executeStatementsBefore = "SET DATABASE REFERENTIAL INTEGRITY FALSE;", executeStatementsAfter = "SET DATABASE REFERENTIAL INTEGRITY TRUE;")
    public void shouldSeedDataSetDisablingContraintsViaStatement() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml")
                        .executeStatementsBefore("SET DATABASE REFERENTIAL INTEGRITY FALSE;")
                        .executeStatementsAfter("SET DATABASE REFERENTIAL INTEGRITY TRUE;"))
                .createDataSet();

        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers join fetch u.tweets join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
    }


    @Test
    /*@DataSet(value = "datasets/yml/users.yml",
            useSequenceFiltering = false,
            tableOrdering = {"USER", "TWEET", "FOLLOWER"},
            executeStatementsBefore = {"DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"})*/
    public void shouldSeedDataSetUsingTableCreationOrder() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml")
                        .useSequenceFiltering(true)
                        .tableOrdering("USER", "TWEET", "FOLLOWER")
                        .executeStatementsBefore("DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"))
                .createDataSet();
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u left join fetch u.tweets left join fetch u.followers").getResultList();
        assertThat(users).hasSize(2);
    }

    @Test
    //@DataSet(value = "datasets/yml/users.yml", strategy = SeedStrategy.TRUNCATE_INSERT, useSequenceFiltering = true)
    public void shouldSeedUserDataSetUsingTruncateInsert() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml")
                        .useSequenceFiltering(true)
                        .strategy(TRUNCATE_INSERT))
                .createDataSet();
        List<User> users = EntityManagerProvider.em("rules-it").createQuery("select u from User u", User.class).getResultList();
        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.get(0).getId()).isEqualTo(1);
        assertThat(users.get(0).getName()).isEqualTo("@realpestano");
        assertThat(users.get(1).getId()).isEqualTo(2);
        assertThat(users.get(1).getName()).isEqualTo("@dbunit");
    }

    @Test
    //@DataSet(value = "datasets/json/users.json")
    public void shouldLoadUsersFromJsonDataset() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/json/users.json"))
                .createDataSet();
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules json example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    //@DataSet(value = "datasets/xml/users.xml")
    public void shouldLoadUsersFromXmlDataset() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/xml/users.xml"))
                .createDataSet();
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules flat xml example", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    //@DataSet(strategy = SeedStrategy.INSERT, value = {"yml/user.yml", "yml/tweet.yml", "yml/follower.yml"}, executeStatementsBefore = {"DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"})
    public void shouldLoadDataFromMultipleDataSets() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/user.yml", "yml/tweet.yml", "yml/follower.yml")
                        .strategy(INSERT)
                        .executeStatementsBefore("DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"))
                .createDataSet();
        User user = (User) EntityManagerProvider.em("rules-it").createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules again!", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }

    @Test
    //@DataSet(strategy = SeedStrategy.INSERT, value = "yml/user.yml, yml/tweet.yml, yml/follower.yml",
    // executeStatementsBefore = {"DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"})
    public void shouldLoadDataFromMultipleDataSetsUsingCommaToSeparateNames() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/user.yml, yml/tweet.yml, yml/follower.yml")
                        .strategy(INSERT)
                        .executeStatementsBefore("DELETE FROM FOLLOWER", "DELETE FROM TWEET", "DELETE FROM USER"))
                .createDataSet();
        User user = (User) EntityManagerProvider.em("rules-it").createQuery("select u from User u join fetch u.tweets join fetch u.followers left join fetch u.followers where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getTweets()).hasSize(1);
        Assert.assertEquals("dbunit rules again!", user.getTweets().get(0).getContent());
        assertThat(user.getFollowers()).isNotNull().hasSize(1);
        Follower expectedFollower = new Follower(2, 1);
        assertThat(user.getFollowers()).contains(expectedFollower);
    }


    @Test
    //@DataSet(value = "datasets/csv/USER.csv", cleanBefore = true)
    public void shouldSeedDatabaseWithCSVDataSet() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/csv/USER.csv")
                        .cleanBefore(true))
                .createDataSet();
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join u.tweets t where t.content = 'dbunit rules!'").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
    }


    @Test
    //@DataSet("xls/users.xls")
    public void shouldSeedDatabaseWithXLSDataSet() {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("xls/users.xls")
                        .cleanBefore(true))
                .createDataSet();
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join u.tweets t where t.content = 'dbunit rules!'").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
    }

    //expected datasets
    /**
     * same as:
     * @DataSet(cleanBefore = true)
     * @ExpectedDataSet(value = "yml/expectedUsers.yml", ignoreCols = "id")
     */
    @Test
    public void shouldMatchExpectedDataSet() throws DatabaseUnitException {
        withConnection(emProvider.connection())
                .cleanDB();
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        tx().begin();
        em().persist(u);
        em().persist(u2);
        tx().commit();

        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/expectedUsers.yml"))
                .expectDataSet(new ExpectedDataSetConfig().ignoreCols("id"));
    }

    @Test
    //@ExpectedDataSet(value = "yml/expectedUsers.yml", ignoreCols = "id")
    public void shouldNotMatchExpectedDataSet() throws DatabaseUnitException {
        withConnection(emProvider.connection())
                .cleanDB();

        User u = new User();
        u.setName("non expected user1");
        User u2 = new User();
        u2.setName("non expected user2");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
        EntityManagerProvider.tx().commit();

        exceptionRule.expect(ComparisonFailure.class);
        exceptionRule.expectMessage("value (table=USER, row=0, col=NAME) expected:<[]expected user1> but was:<[non ]expected user1>");

        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/expectedUsers.yml"))
                .expectDataSet(new ExpectedDataSetConfig().ignoreCols("id"));
    }

    @Test
    //@DataSet(cleanBefore = true)
    //@ExpectedDataSet(value = "yml/expectedUsersRegex.yml")
    public void shouldMatchExpectedDataSetUsingRegex() throws DatabaseUnitException {
        withConnection(emProvider.connection())
                .cleanDB();
        User u = new User();
        u.setName("expected user1");
        User u2 = new User();
        u2.setName("expected user2");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(u);
        EntityManagerProvider.em().persist(u2);
        EntityManagerProvider.tx().commit();
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/expectedUsersRegex.yml"))
                .expectDataSet();
    }


    @Test
    //@DataSet(value = "yml/user.yml", transactional = true)
    //@ExpectedDataSet(value = "yml/expectedUsersContains.yml", compareOperation = CompareOperation.CONTAINS)
    public void shouldMatchExpectedDataSetContains() throws DatabaseUnitException {
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/user.yml").cleanBefore(true))
                .createDataSet();
        tx().begin();
        User u = new User();
        u.setId(3);
        u.setName("@dbrider");
        em().persist(u);
        tx().commit();
        withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/expectedUsersContains.yml"))
                .expectDataSet(new ExpectedDataSetConfig().compareOperation(CompareOperation.CONTAINS));
    }

}
