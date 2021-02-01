package com.github.database.rider.core.replacers;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.configuration.ExpectedDataSetConfig;
import com.github.database.rider.core.dsl.RiderDSL;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(replacers = CustomReplacer.class)
public class CustomReplacementIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    @Test
    @DataSet(value = "datasets/yml/custom-replacements.yml", disableConstraints = true)
    public void shouldReplaceOnlyFoo() {
        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");
        tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '2'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");//was not replaced because CustomReplacerBar is not used
    }

    @Test
    @DataSet(value = "datasets/yml/custom-replacements.yml", disableConstraints = true, replacers = CustomReplacerBar.class)
    public void shouldReplaceOnlyBar() {
        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("FOO"); //was not replaced because CustomReplacer was not used (overriden @dataset replacer)
        tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '2'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAZ");
    }

    @Test
    @DataSet(value = "datasets/yml/custom-replacements.yml", disableConstraints = true, replacers = {CustomReplacer.class, CustomReplacerBar.class})
    public void shouldReplaceFooAndBar() {
        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");
        tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '2'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAZ");
    }

    //expectedDataSetReplacer

    @Test
    @DataSet(transactional = true, disableConstraints = true, cleanBefore = true)
    @ExpectedDataSet(value = "datasets/yml/custom-replacements.yml", ignoreCols = {"id", "user_id"})
    public void shouldReplaceOnlyFooInExpectedDataSet() {
        Tweet tweet1 = new Tweet().setContent("BAR");
        Tweet tweet2 = new Tweet().setContent("BAR");
        em().persist(tweet1);
        em().persist(tweet2);
    }

    @Test
    @DataSet(disableConstraints = true, replacers = CustomReplacerBar.class, cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "datasets/yml/custom-replacements.yml", replacers = CustomReplacerBar.class, ignoreCols = {"id", "user_id"})
    public void shouldReplaceOnlyBarInExpectedDataSet() {
        Tweet tweet1 = new Tweet().setContent("FOO");
        Tweet tweet2 = new Tweet().setContent("BAZ");
        em().persist(tweet1);
        em().persist(tweet2);
    }

    @Test
    @DataSet(disableConstraints = true, replacers = CustomReplacerBar.class, cleanBefore = true, transactional = true)
    @ExpectedDataSet(value = "datasets/yml/custom-replacements.yml", replacers = {CustomReplacer.class, CustomReplacerBar.class}, ignoreCols = {"id", "user_id"})
    public void shouldReplaceFooAndBarInExpectedDataSet() {
        Tweet tweet1 = new Tweet().setContent("BAR");
        Tweet tweet2 = new Tweet().setContent("BAZ");
        em().persist(tweet1);
        em().persist(tweet2);
    }

    @Test
    public void shouldReplaceFooUsingRiderDSL() {
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/custom-replacements.yml")
                        .disableConstraints(true))
                .withDBUnitConfig(new DBUnitConfig().addDBUnitProperty("replacers", Arrays.asList(new CustomReplacer())))
                .createDataSet();
        Tweet tweet = (Tweet) em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");
    }

    @Test
    public void shouldReplaceFooInExpectedDataSetUsingRiderDSL() throws DatabaseUnitException {
        //given a clean db
        RiderDSL.withConnection(emProvider.connection())
                .cleanDB();
        //when I insert two tweets
        em().getTransaction().begin();
        Tweet tweet1 = new Tweet().setContent("BAR");
        Tweet tweet2 = new Tweet().setContent("BAR");
        em().persist(tweet1);
        em().persist(tweet2);
        em().getTransaction().commit();
        //then database state should be
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/custom-replacements.yml")
                        .disableConstraints(true))
                .withDBUnitConfig(new DBUnitConfig().addDBUnitProperty("replacers", Arrays.asList(new CustomReplacer())))
                .expectDataSet(new ExpectedDataSetConfig().ignoreCols("id", "user_id"));
    }


}
