package com.github.database.rider.core;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.model.Tweet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.util.EntityManagerProvider;

/**
 * Created by vinsgithub on 10/08/2018.
 */
// tag::expectedDeclaration[]
@RunWith(JUnit4.class)
@DBUnit(cacheConnection = true)
public class ExpectedDataSetReplacersIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


// end::expectedDeclaration[]

    // tag::expected[]
    @Test
    @DataSet(cleanBefore = true)//<1>
    @ExpectedDataSet(value = "yml/null-replacements.yml", ignoreCols = "id", replacers=NullReplacer.class)
    public void shouldMatchExpectedDataSetNullReplaced() {
        EntityManagerProvider instance = EntityManagerProvider.newInstance("rules-it");
        User u = new User(1);
        Tweet t = new Tweet();
        t.setId("1");
        t.setContent(null);
        t.setUser(u);
        Tweet t2 = new Tweet();
        t2.setId("2");
        t2.setContent("null");
        t2.setUser(u);
        instance.tx().begin();
        instance.em().persist(u);
        instance.em().persist(t);
        instance.em().persist(t2);
        instance.tx().commit();
    }
    // end::expected[]


}
