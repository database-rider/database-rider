package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.dataset.builder.ColumnSpec;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import com.github.database.rider.junit5.model.Tweet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.model.User_;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.database.rider.junit5.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitPlatform.class)
@ExtendWith(DBUnitExtension.class)
@DataSet(provider = DataSetProviderIt.TweetDataSetProvider.class, cleanBefore = true)
public class DataSetProviderIt {

    private ConnectionHolder connectionHolder = () ->
            EntityManagerProvider.instance("junit5-pu").clear().connection();

    @Test
    @DataSet(provider = UserDataSetProvider.class, cleanBefore = true)
    public void shouldSeedDatabaseProgrammatically() {
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u ").getResultList();
        assertThat(users).
            isNotNull().
            isNotEmpty().hasSize(2).
            extracting("name").
            contains("@dbunit", "@dbrider");
    }

    @Test
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
    @DataSet(provider = UsersWithBrokenReferentialConstraintProvider.class, disableConstraints = true)
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

    public static class UserDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user").
                    row()
                        .column("id", 1)
                        .column("name", "@dbunit")
                    .table("user").
                    row()
                        .column("id", 2)
                        .column("name", "@dbrider");
            return builder.build();
        }
    }

    public static class UsersWithBrokenReferentialConstraintProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            ColumnSpec id = ColumnSpec.of("ID");
            ColumnSpec name = ColumnSpec.of("NAME");
            builder.table("USER")
                    .row()
                        .column("ID", 1)
                        .column(name, "@realpestano")
                    .table("USER")
                    .row()
                        .column(id, 2).column("NAME", "@dbunit")
                   .table("TWEET")
                   .row()
                        .column("ID", "abcdef12345")
                        .column("CONTENT", "dbunit rules!")
                        .column("DATE", "[DAY,NOW]")
                        .column("USER_ID", 9999)
                .build();

            return builder.build();
        }
    }

    public static class UserDataSetWithMetaModelProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("user")
                    .row()
                        .column(User_.id, 1)
                        .column(User_.name, "@dbunit")
                    .table("user")
                    .row()
                        .column(User_.id, 2)
                        .column(User_.name, "@dbrider");
            return builder.build();
        }
    }

    public static class ExpectedUserProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
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
        public IDataSet provide() throws DataSetException {
            DataSetBuilder builder = new DataSetBuilder();
            builder.table("TWEET")
                    .row()
                        .column("ID", "abcdef12345")
                        .column("CONTENT", "dbrider rules!")
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

}
