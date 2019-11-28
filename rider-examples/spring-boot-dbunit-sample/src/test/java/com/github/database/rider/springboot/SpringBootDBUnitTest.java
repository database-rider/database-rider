package com.github.database.rider.springboot;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot.models.User;
import com.github.database.rider.springboot.models.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 13/09/16.
 */
@DBRider
@SpringBootTest
public class SpringBootDBUnitTest {

    @Autowired
    private UserRepository userRepository;

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
        userRepository.delete(userRepository.findById(2L).get());
        //assertThat(userRepository.count()).isEqualTo(2); //assertion is made by @ExpectedDataset
    }


    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @ExpectedDataSet("user.yml")
    public void shouldInsertUser() throws Exception {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(0);
        userRepository.save(new User("newUser@gmail.com", "new user"));
        //assertThat(userRepository.count()).isEqualTo(1); //assertion is made by @ExpectedDataset
    }
}
