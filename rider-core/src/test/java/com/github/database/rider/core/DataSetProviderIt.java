package com.github.database.rider.core;

import com.github.database.rider.core.DataSetProviderIt.TweetDataSetProvider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.dataset.builder.ColumnSpec;
import com.github.database.rider.core.dataset.builder.DataRowBuilder;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.model.User_;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DataSet(provider = TweetDataSetProvider.class, cleanBefore = true)
public class DataSetProviderIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @DataSet(provider = UserDataSetProvider.class, cleanBefore = true)
    @ExportDataSet(outputName = "out.yml")
    public void shouldSeedDatabaseProgrammatically() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
    }

    @Test
    @ExportDataSet(outputName = "out2.yml")
    @DataSet(provider = UserDataSetWithMetaModelProvider.class, cleanBefore = true)
    public void shouldSeedDatabaseProgrammaticallyUsingMetaModel() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
    }

    @Test
    @DataSet(provider = UsersWithBrokenReferentialConstraintProvider.class, disableConstraints = true, cleanBefore = true)
    public void shouldSeedDataSetDisablingContraints() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    @DataSet(provider = UserDataSetProvider.class, cleanBefore = true, transactional = true)
    @ExpectedDataSet(provider = ExpectedUserProvider.class, ignoreCols = "id")
    public void shouldMatchExpectedDataSetUsingDataSetProvider() {
        Long count = (Long) EntityManagerProvider.em().createQuery("select count(u) from User u ").getSingleResult();
        assertThat(count).isEqualTo(2);
        em().remove(EntityManagerProvider.em().find(User.class, 1L));
        //assertThat(count).isEqualTo(1); //assertion in expectedDataSet
    }

    @Test
    public void shouldSeedDataSetUsingClassLevelDataSetProvider() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = 'abcdef12345'").getSingleResult();
        assertThat(tweet).isNotNull()
            .extracting("content")
            .contains("dbrider rules!");
    }

    @Test
    @DataSet(provider = CompositeDataSetProvider.class)
    public void shouldSeedDataSetUsingCompositeDataSetProvider() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = 'abcdef12345'").getSingleResult();
        assertThat(tweet).isNotNull()
            .extracting("content")
            .contains("dbrider rules!");
    }

    @Test
    public void shouldSeedDatabaseUsingDataSetProviderWithoutAnnotatation() throws SQLException {
        try (Connection conn = EntityManagerProvider.instance("executor-it").connection()) {
            DataSetExecutor executor = DataSetExecutorImpl.instance("executor-name", new ConnectionHolderImpl(conn));
            DataSetConfig DataSetConfig = new DataSetConfig()
                .datasetProvider(UserDataSetProvider.class)
                .disableConstraints(true);
            executor.clearDatabase(DataSetConfig);
            executor.createDataSet(DataSetConfig);
            List<User> users = EntityManagerProvider.em("executor-it").createQuery("select u from User u ").getResultList();
            assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@dbunit", "@dbrider");
        }

    }

    @Test
    @DataSet(provider = ReuseRowsAndDataSetsProvider.class, cleanBefore = true)
    public void shouldReuseRowsAndDataSets() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("user1", "user2");
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = 'abcdef12345'").getSingleResult();
        assertThat(tweet).isNotNull()
                .extracting("content")
                .contains("dbrider rules!");
    }


    @Test
    @DataSet(provider = DefaultValueDataSetProvider.class)
    public void shouldSeedDatabaseWithDefaultValues() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@realpestano", "DEFAULT");

    }

    public static class UserDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user").column("id", 1)
                .column("name", "@dbunit")
                .table("user").column("id", 2)
                .column("name", "@dbrider").build();
            return builder.build();
        }
    }

    public static class UsersWithBrokenReferentialConstraintProvider implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            ColumnSpec id = ColumnSpec.of("ID");
            ColumnSpec name = ColumnSpec.of("NAME");
            IDataSet dataSet = builder.table("USER").column("ID", 1)
                .column(name, "@realpestano")
                .row()
                .column(id, 2).column("NAME", "@dbunit")
                .table("TWEET")
                .column("ID", "abcdef12345").column("CONTENT", "dbunit rules!")
                .column("DATE", "[DAY,NOW]")
                .table("FOLLOWER").column(id, 1)
                .column("USER_ID", 9999).column("FOLLOWER_ID", 9999)
                .build();
            return dataSet;
        }
    }


    public static class UserDataSetWithMetaModelProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            return builder.table("user")
                    .column(User_.id, 1)
                    .column(User_.name, "@dbunit")
                .row()
                    .column(User_.id, 2)
                    .column(User_.name, "@dbrider").build();
        }
    }

    public static class ExpectedUserProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user").column("id", 2)
                .column("name", "@dbrider");
            return builder.build();
        }
    }

    public static class TweetDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("TWEET")
                .column("ID", "abcdef12345").column("CONTENT", "dbrider rules!")
                .column("DATE", "[DAY,NOW]");
            return builder.build();
        }
    }

    public static class CompositeDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            IDataSet userDataSet = new UserDataSetProvider().provide();
            IDataSet tweetDataSet = new TweetDataSetProvider().provide();
            return new CompositeDataSet(userDataSet, tweetDataSet);
        }

    }

    public static class ReuseRowsAndDataSetsProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            DataRowBuilder user1Row = new DataSetBuilder().table("USER")
                    .column("id", "1")
                    .column("name", "user1");
            DataRowBuilder user2Row = new DataSetBuilder().table("USER")
                    .column("id", "2")
                    .column("name", "user2");

            IDataSet iDataSet = builder.add(user1Row).add(user2Row)
                    .addDataSet(new TweetDataSetProvider().provide())
                    .build();
            return iDataSet;
        }

    }

    public static class DefaultValueDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder()
                    .defaultValue("NAME", "DEFAULT");
            ColumnSpec id = ColumnSpec.of("ID");
            return builder.table("USER").column(id, 1)
                    .column("NAME", "@realpestano")
                    .row()
                    .column(id, 2)
                    .build();
        }

    }

}
