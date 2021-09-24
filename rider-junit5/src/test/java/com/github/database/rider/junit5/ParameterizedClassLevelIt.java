package com.github.database.rider.junit5;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Java6Assertions.assertThat;

@DataSet(value = "users.yml", cleanBefore = true)
@RunWith(JUnitPlatform.class)
@ExtendWith(DBUnitExtension.class)
public class ParameterizedClassLevelIt {

    private ConnectionHolder connectionHolder = () ->
            com.github.database.rider.core.util.EntityManagerProvider.instance("junit5-pu").clear().connection();


    @ParameterizedTest
    @CsvSource({"1,@realpestano", "2,@dbunit"})
    public void shouldSeedDataSet(String id, String name) {
        User user = (User) EntityManagerProvider.em().createQuery("select u from User u where u.id = " + id).getSingleResult();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
    }
}
