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
import com.github.database.rider.core.dataset.builder.RowBuilder;
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


    // tag::signature[]
    @Test
    @DataSet(provider = UserDataSetProvider.class, //<1>
              cleanBefore = true)
    public void shouldSeedDatabaseProgrammatically() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
    }
    // end::signature[]

    // tag::signature2[]
    @Test
    @DataSet(provider = UserDataSetProviderWithColumnsSyntax.class)
    public void shouldSeedDatabaseUsingDataSetProviderWithColumnsSyntax() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@dbunit", "@dbrider");
    }
    // end::signature2[]

    @Test
    @ExportDataSet(outputName = "target/out2.yml")
    @DataSet(provider = UserDataSetWithMetaModelProvider.class, cleanBefore = true)
    public void shouldSeedDatabaseProgrammaticallyUsingMetaModel() {
        List<User> users = em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
    }

    @Test
    @DataSet(provider = UserDataSetWithMetaModelUsingColumnsSyntax.class, cleanBefore = true)
    public void shouldSeedDatabaseUsingMetaModelWithColumnsSysntax() {
        List<User> users = em().createQuery("select u from User u ").getResultList();
        assertThat(users).
                isNotNull().
                isNotEmpty().hasSize(2).
                extracting("name").
                contains("@dbunit", "@dbrider");
    }

    @Test
    @DataSet(provider = BrokenReferentialConstraintProvider.class, disableConstraints = true)
    public void shouldSeedDataSetDisablingContraints() {
        List<User> users = em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(3).
            extracting("name").
            contains("@dbunit", "@dbrider", "@new row");
    }
    
    @Test
    @DataSet(provider = UserDataSetProvider.class, cleanBefore = true, transactional = true)
    @ExpectedDataSet(provider = ExpectedUserProvider.class, ignoreCols = "id")
    public void shouldMatchExpectedDataSetUsingDataSetProvider() {
        Long count = (Long) EntityManagerProvider.em().createQuery("select count(u) from User u ").getSingleResult();
        assertThat(count).isEqualTo(2);
        em().remove(EntityManagerProvider.em().find(User.class, 1L));
        //assertThat(count).isEqualTo(1); //assertion is in expectedDataSet
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

    // tag::provider[]
    public static class UserDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user")//<1>
                    .row() //<2>
                        .column("id", 1) //<3>
                        .column("name", "@dbunit")
                    .row() //<4>
                        .column("id", 2)
                        .column("name", "@dbrider").build();
            return builder.build(); //<5>
        }
    }
    // end::provider[]

    // tag::provider2[]
    public static class UserDataSetProviderWithColumnsSyntax implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            IDataSet iDataSet = builder.table("user") //<1>
                    .columns("id", "name") //<2>
                    .values(1,"@dbunit") //<3>
                    .values(2,"@dbrider").build();
            return iDataSet;
        }
    }
    // end::provider2[]

    public static class UserDataSetWithMetaModelProvider implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            return builder.table("user")
                    .row()
                        .column(User_.id, 1)
                        .column(User_.name, "@dbunit")
                    .row()
                        .column(User_.id, 2)
                        .column(User_.name, "@dbrider").build();
        }
    }

    public static class UserDataSetWithMetaModelUsingColumnsSyntax implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            return builder.table("user")
                    .columns(User_.id,User_.name)
                    .values(1, "@dbunit")
                    .values(2, "@dbrider")
                   .build();
        }
    }

    public static class ExpectedUserProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user")
                    .row()
                        .column("id", 2)
                        .column("name", "@dbrider");
            return builder.build();
        }
    }

    public static class TweetDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide()  {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("TWEET")
                   .row()
                        .column("ID", "abcdef12345").column("CONTENT", "dbrider rules!")
                        .column("DATE", "[DAY,NOW]");
            return builder.build();
        }
    }
    
    public static class BrokenReferentialConstraintProvider implements DataSetProvider {

        @Override
        public IDataSet provide() {
            DataSetBuilder builder = new DataSetBuilder();
            ColumnSpec id = ColumnSpec.of("ID");
            ColumnSpec name = ColumnSpec.of("NAME");
            IDataSet dataSet = builder
                    .table("USER") //start adding rows to 'USER' table
                    .row()
                        .column("ID", 1)
                        .column(name, "@dbunit")
                    .row() //keeps adding rows to the current table
                        .column(id, 2)
                        .column("NAME", "@dbrider")
                    .table("TWEET") //starts adding rows to 'TWEET' table
                    .row()
                        .column("ID", "abcdef12345")
                        .column("CONTENT", "dbunit rules!")
                        .column("DATE", "[DAY,NOW]")
                    .table("FOLLOWER")
                    .row()
                        .column(id, 1)
                        .column("USER_ID", 9999)
                        .column("FOLLOWER_ID", 9999)
                    .table("USER")// we still can add rows to table already added to the dataset
                    .row()
                       .column("ID", 3)
                       .column(name, "@new row")
                .build();
            return dataSet;
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
            RowBuilder user1Row = new DataSetBuilder().table("USER")
                    .row()
                        .column("id", "1")
                        .column("name", "user1");
            RowBuilder user2Row = new DataSetBuilder().table("USER")
                    .row()
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
            return builder.table("USER")
                    .row()
                        .column(id, 1)
                        .column("NAME", "@realpestano")
                    .row()
                        .column(id, 2)
                    .build();
        }

    }

}
