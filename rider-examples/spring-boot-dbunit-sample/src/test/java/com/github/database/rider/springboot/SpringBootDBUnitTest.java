package com.github.database.rider.springboot;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot.model.user.User;
import com.github.database.rider.springboot.model.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 13/09/16.
 */
@DBRider
@SpringBootTest
@DBUnit(cacheConnection = false, leakHunter = true)
public class SpringBootDBUnitTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    @DataSet("users.yml")
    public void setUpUsers() {
    }

    @Test
    public void shouldListUsers() {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findByEmail("springboot@gmail.com")).isEqualTo(new User(3));
    }

    @Test
    @ExpectedDataSet("expectedUsers.yml")
    public void shouldDeleteUser() {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(3);
        userRepository.findById(2L).ifPresent(userRepository::delete);
        //assertThat(userRepository.count()).isEqualTo(2); //assertion is made by @ExpectedDataset
    }


    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @ExpectedDataSet("user.yml")
    public void shouldInsertUser() {
        assertThat(userRepository).isNotNull();
        assertThat(userRepository.count()).isEqualTo(0);
        userRepository.save(new User("newUser@gmail.com", "new user"));
        //assertThat(userRepository.count()).isEqualTo(1); //assertion is made by @ExpectedDataset
    }
}
