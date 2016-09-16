package com.github.database.rider.springboot;

import com.github.database.rider.DBUnitRule;
import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.springboot.models.User;
import com.github.database.rider.api.dataset.ExpectedDataSet;
import com.github.database.rider.springboot.models.UserRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 13/09/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootDBUnitTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.
            instance(() -> jdbcTemplate.getDataSource().getConnection());



    /** users.yml
     * USERS:
     - ID: 1
     EMAIL: "dbunit@gmail.com"
     NAME: "dbunit"
     - ID: 2
     EMAIL: "rmpestano@gmail.com"
     NAME: "rmpestano"
     - ID: 3
     EMAIL: "springboot@gmail.com"
     NAME: "springboot"
     */
    @Test
    @DataSet("users.yml")
    public void shouldListUsers() throws Exception {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findByEmail("springboot@gmail.com")).isEqualTo(new User(3));
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet("expectedUsers.yml")
    public void shouldDeleteUser() throws Exception {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(3);
        userRepository.delete(userRepository.findOne(2L));
        //assertThat(userRepository.count()).isEqualTo(2); //assertion is made by @ExpectedDataset
    }


    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @ExpectedDataSet("user.yml")
    public void shouldInsertUser() throws Exception {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(0);
        userRepository.save(new User("newUser@gmail.com","new user"));
        //assertThat(userRepository.count()).isEqualTo(1); //assertion is made by @ExpectedDataset
    }

}
