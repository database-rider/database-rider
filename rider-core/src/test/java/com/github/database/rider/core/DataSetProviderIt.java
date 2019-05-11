package com.github.database.rider.core;

import com.github.database.rider.core.DataSetProviderIt.TweetDataSetProvider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.dataset.builder.RiderDataSetBuilder;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User_;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.dbunit.dataset.builder.ColumnSpec;

@RunWith(JUnit4.class)
@DataSet(provider = TweetDataSetProvider.class, cleanBefore = true)
public class DataSetProviderIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


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


    public static class UserDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            RiderDataSetBuilder builder = new RiderDataSetBuilder(true);
            builder.newRow("user").with("id", 1)
                    .with("name", "@dbunit").add()
                    .newRow("user").with("id", 2)
                    .with("name", "@dbrider").add();
            return builder.build();
        }
    }
    
    public static class UsersWithBrokenReferentialConstraintProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            RiderDataSetBuilder builder = new RiderDataSetBuilder();
            ColumnSpec<Integer> id = ColumnSpec.newColumn("ID");
             ColumnSpec<String> name = ColumnSpec.newColumn("NAME");
            builder.newRow("USER").with("ID", 1)
                .with(name, "@realpestano")
                .add().newRow("USER")
                .with(id, 2).with("NAME", "@dbunit")
                .add().newRow("TWEET")
                .with("ID", "abcdef12345").with("CONTENT", "dbunit rules!")
                .with("DATE", "[DAY,NOW]")
                .add().newRow("FOLLOWER").with(id, 1)
                .with("USER_ID", 9999).with("FOLLOWER_ID", 9999)
                .add().build();
            
            return builder.build();
        }
    }

    public static class UserDataSetWithMetaModelProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            RiderDataSetBuilder builder = new RiderDataSetBuilder(true);
            builder.newRow("user").with(User_.id, 1)
                    .with(User_.name, "@dbunit").add()
                    .newRow("user").with(User_.id, 2)
                    .with(User_.name, "@dbrider").add();
            return builder.build();
        }
    }
    
    public static class ExpectedUserProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            RiderDataSetBuilder builder = new RiderDataSetBuilder(true);
            builder.newRow("user").with("id", 2)
                    .with("name", "@dbrider").add();
            return builder.build();
        }
    }
    
    public static class TweetDataSetProvider implements DataSetProvider {

        @Override
        public IDataSet provide() throws DataSetException {
            RiderDataSetBuilder builder = new RiderDataSetBuilder(true);
            builder.newRow("TWEET")
                .with("ID", "abcdef12345").with("CONTENT", "dbrider rules!")
                .with("DATE", "[DAY,NOW]").add();
            return builder.build();
        }
    }

}
