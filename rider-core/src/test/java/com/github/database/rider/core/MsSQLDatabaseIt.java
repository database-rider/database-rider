package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.lowercase.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.testcontainers.containers.MSSQLServerContainer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DBUnit(caseSensitiveTableNames = true, escapePattern = "\"?\"")
public class MsSQLDatabaseIt {
    private static final MSSQLServerContainer mssqlserver = new MSSQLServerContainer();

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("mssqlserver-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());


    @BeforeClass
    public static void setupContainer() {
        mssqlserver.start();
    }

    @AfterClass
    public static void shutdown() {
        mssqlserver.stop();
    }

    @Test
    @DataSet(value = "datasets/yml/lowercaseUsers.yml")
    public void shouldSeedDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = 1").getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }
}
