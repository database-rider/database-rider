package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class ParameterizedIt {

    @Rule
    public EntityManagerProvider emProvider = EntityManagerProvider.instance("param-it");

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(emProvider.connection());

    Integer id;
    String name;

    public ParameterizedIt(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[] {1,"@realpestano"},
                new Object[] {2,"@dbunit"}
        );
    }

    @Test
    @DataSet(value = "datasets/yml/users.yml", strategy = SeedStrategy.REFRESH)
    public void shouldSeedDataSet() {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = "+id).getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
    }

}
