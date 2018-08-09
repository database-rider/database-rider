package com.github.database.rider.core.replacers;


import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DBUnit(cacheConnection = true)
public class NullReplacementsIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("rules-it", emProvider.connection());

    @Test
    @DataSet(value = "datasets/yml/null-replacements.yml", disableConstraints = true, executorId = "rules-it")
    public void shouldReplaceNullPlaceholder() {
        Tweet tweet = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '1'").getSingleResult();
        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isNull();

        Tweet tweet2 = (Tweet) EntityManagerProvider.em().createQuery("select t from Tweet t where t.id = '2'").getSingleResult();
        assertThat(tweet2).isNotNull();
        assertThat(tweet2.getContent()).isNotNull().isEqualTo("null");
    }


}
