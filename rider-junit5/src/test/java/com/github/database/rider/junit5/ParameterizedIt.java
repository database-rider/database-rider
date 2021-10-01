package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.util.EntityManagerProvider;
import com.github.database.rider.junit5.incubating.DBRider;
import com.github.database.rider.junit5.model.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Java6Assertions.assertThat;

@DBRider
@RunWith(JUnitPlatform.class)
public class ParameterizedIt {

    private ConnectionHolder connectionHolder = () ->
            com.github.database.rider.core.util.EntityManagerProvider.instance("junit5-pu").clear().connection();

    @DataSet(value = "users.yml", cleanBefore = true)
    @ParameterizedTest
    @CsvSource({"1,@realpestano", "2,@dbunit"})
    public void shouldSeedDataSet(Integer id, String name) {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = " + id).getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
    }

}
