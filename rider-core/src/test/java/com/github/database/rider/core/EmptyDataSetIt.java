package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.exporter.BuilderType;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by rmpestano on 8/21/16.
 */
@RunWith(JUnit4.class)
@DBUnit(cacheConnection = true)
public class EmptyDataSetIt {

    EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public TestRule theRule = RuleChain.outerRule(emProvider).
            around(DBUnitRule.instance(emProvider.connection()));


    @BeforeClass
    public static void init(){
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        EntityManagerProvider.tx("rules-it").begin();
        EntityManagerProvider.em("rules-it").persist(user);
        EntityManagerProvider.tx("rules-it").commit();
    }

    @Test
    @DataSet(value = "yml/empty.yml")
    public void shouldSeedDatabaseWithEmptyDataSet() {
        long count = (long) EntityManagerProvider.em().createQuery("select count(u.id) from User u").getSingleResult();
        assertThat(0L).isEqualTo(count);
        User user = new User();
        user.setName("user");
        user.setName("@rmpestano");
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().persist(user);
        EntityManagerProvider.tx().commit();
        User insertedUser = (User) EntityManagerProvider.em().createQuery("select u from User u where u.name = '@rmpestano'").getSingleResult();
        assertThat(insertedUser).isNotNull();
        assertThat(insertedUser.getId()).isNotNull();
    }

    @Test
    @DataSet("yml/empty.yml")
    @ExpectedDataSet("yml/empty.yml")
    @ExportDataSet(builderType = BuilderType.COLUMNS_VALUES)
    public void shouldSeedAndExpectEmptyDataSet() {
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSet() {
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,2L));
        EntityManagerProvider.tx().commit();
    }

    @Test
    @DataSet(value = "yml/user.yml", disableConstraints = true, transactional = true)
    @ExpectedDataSet(value = "yml/empty.yml")
    public void shouldMatchEmptyYmlDataSetWithTransaction() {
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,2L));
    }


    @Test
    @DataSet(value = "json/user.json", disableConstraints = true)
    @ExpectedDataSet(value = "json/empty.json")
    public void shouldMatchEmptyJsonDataSet() {
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,2L));
        EntityManagerProvider.tx().commit();
        EntityManagerProvider.em().createQuery("select u from User u").getResultList();
    }

    @Test
    @DataSet(value = "xml/user.xml", disableConstraints = true)
    @ExpectedDataSet(value = "xml/empty.xml")
    public void shouldMatchEmptyXmlDataSet() {
        EntityManagerProvider.tx().begin();
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,1L));
        EntityManagerProvider.em().remove(EntityManagerProvider.em().find(User.class,2L));
        EntityManagerProvider.tx().commit();
    }
}
