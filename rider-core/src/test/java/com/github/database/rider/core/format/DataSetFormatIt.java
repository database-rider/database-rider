package com.github.database.rider.core.format;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DataSetFormatIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("rules-it");

    @Rule
    public TestRule theRule = RuleChain.outerRule(emProvider).
            around(DBUnitRule.instance(emProvider.connection()));


//tag::yml[]
    @Test
    @DataSet("yml/users.yml")
    public void shouldSeedDatabaseWithYAMLDataSet() {
       List<User> users = em().createQuery("select u from User u").getResultList();
       assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::yml[]

//tag::json[]
    @Test
    @DataSet("json/users.json")
    public void shouldSeedDatabaseWithJSONDataSet() {
       List<User> users = em().createQuery("select u from User u").getResultList();
       assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::json[]


//tag::xml[]
    @Test
    @DataSet("xml/users.xml")
    public void shouldSeedDatabaseWithXMLDataSet() {
       List<User> users = em().createQuery("select u from User u").getResultList();
       assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::xml[]

//tag::xls[]
    @Test
    @DataSet("xls/users.xls")
    public void shouldSeedDatabaseWithXLSDataSet() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::xls[]

//tag::csv[]
    @Test
    @DataSet("datasets/csv/USER.csv") //<1>
    public void shouldSeedDatabaseWithCSVDataSet() {
        List<User> users = em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty().hasSize(2);
    }
//end::csv[]

}
