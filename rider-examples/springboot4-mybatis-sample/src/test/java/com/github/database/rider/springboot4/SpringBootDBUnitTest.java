package com.github.database.rider.springboot4;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot4.entity.user.Users;
import com.github.database.rider.springboot4.service.UsersService;

/**
 * Created by pestano on 13/09/16.
 * Declare @SpringBootTest first to ensure Spring context loads prior to DBRider extension execution.
 */
@SpringBootTest
@DBRider
public class SpringBootDBUnitTest {

    @Autowired
    private UsersService usersService;

    @Test
    @DataSet("users.yml")
    @Transactional
    public void shouldListUsers() {
        assertThat(this.usersService).isNotNull();
        assertThat(this.usersService.count()).isEqualTo(3);
        assertThat(this.usersService.findByEmail("springboot@gmail.com").getId()).isEqualTo(3L);
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet("expectedUsers.yml")
    @Transactional
    public void shouldDeleteUser() {
        assertThat(this.usersService).isNotNull();
        assertThat(this.usersService.count()).isEqualTo(3);
        this.usersService.findById(2L).ifPresent(this.usersService::delete);
    }

    @Test
    @DataSet("emptyUsers.yml")
    @ExpectedDataSet("user.yml")
    @Transactional
    public void shouldInsertUser() {
        assertThat(this.usersService).isNotNull();
        assertThat(this.usersService.count()).isEqualTo(0);
        this.usersService.save(new Users("newUser@gmail.com", "new user"));
    }
}
