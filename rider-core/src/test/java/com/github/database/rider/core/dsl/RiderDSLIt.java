package com.github.database.rider.core.dsl;

import com.github.database.rider.core.DataSetProviderIt;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.model.Follower;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.api.dataset.SeedStrategy.INSERT;
import static com.github.database.rider.core.api.dataset.SeedStrategy.TRUNCATE_INSERT;
import static com.github.database.rider.core.dsl.RiderDSL.DataSetConfigDSL.withDataSetConfig;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RiderDSLIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Test
    public void shouldCreateDataSetUsingDSL() {
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml"))
                .createDataSet();

        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2)
                .extracting("name")
                .contains("@realpestano", "@dbunit");

    }

    @Test
    public void shouldCreateMultipleDataSetsReusingDSLInstance() {
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/users.yml"))
                .createDataSet();

        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).hasSize(2)
                .extracting("name")
                .contains("@realpestano", "@dbunit");


        RiderDSL.withConnection()
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
    //@DataSet(value = "yml/lowercaseUsers.yml") // note that hsqldb tables are in uppercase
    //@DBUnit(caseSensitiveTableNames = false)
    public void shouldListUsersWithCaseInSensitiveTableNames() {
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/lowercaseUsers.yml"))
                .withDBUnitConfig(new DBUnitConfig().addDBUnitProperty("caseSensitiveTableNames", false))
                .createDataSet();
        List<com.github.database.rider.core.model.lowercase.User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    //@DataSet(value = "datasets/yml/user.yml", cleanBefore = true)
    public void shouldSeedDatabase() {
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("yml/user.yml","yml/tweet.yml","yml/follower.yml")
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
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
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("xls/users.xls")
                        .cleanBefore(true))
                .createDataSet();
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u join u.tweets t where t.content = 'dbunit rules!'").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("@realpestano");
    }

}
