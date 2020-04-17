package com.github.database.rider.core.replacers;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dsl.RiderDSL;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(replacers = CustomReplacer.class)
public class CustomReplacementIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it", emProvider.connection());

    @Test
    @DataSet(value = "datasets/yml/custom-replacements.yml", disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceFoo() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");
    }

    @Test
    public void shouldReplaceFooUsingRiderDSL() {
        RiderDSL.withConnection(emProvider.connection())
                .withDataSetConfig(new DataSetConfig("datasets/yml/custom-replacements.yml")
                         .disableConstraints(true))
                .withDBUnitConfig(new DBUnitConfig().addDBUnitProperty("replacers", Arrays.asList(new CustomReplacer())))
                .createDataSet();
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNotNull().isEqualTo("BAR");
    }

}
