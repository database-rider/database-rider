package com.github.database.rider.springboot;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.configuration.ExpectedDataSetConfig;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot.model.user.User;
import com.github.database.rider.springboot.model.user.UserRepository;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.github.database.rider.core.dsl.RiderDSL.withConnection;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@DBRider
public class SpringBootDataJpaRollbackTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DataSource dataSource;

    @BeforeAll
    @DataSet("users.yml")
    static void beforeAll() {

    }

    @Disabled("Enable once we make expectedDataSet work with @DataJpaTest: https://github.com/database-rider/database-rider/issues/482")
    @Test
    @DataSet(executeScriptsBefore = "/scripts/addUser.sql")
    @ExpectedDataSet(value = "expectedAllUsers.yml", ignoreCols = "id")
    public void shouldListUsers() {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(4);
        assertThat(userRepository.findByEmail("junit5@mail.com")).isEqualTo(new User(99));
        userRepository.save(new User("bdd@cucumber.com", "cucumber"));
    }

    /*@AfterEach
    void after() throws SQLException, DatabaseUnitException {
        userRepository.findAll()
                        .forEach(System.out::println);
        withConnection(dataSource.getConnection())
                .withDataSetConfig(new DataSetConfig("expectedUsersAfterRollback.yml"))
                .expectDataSet(new ExpectedDataSetConfig().ignoreCols("id"));
    }*/
}
