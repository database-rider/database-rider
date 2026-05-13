package com.github.database.rider.springboot4;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot4.entity.company.Company;
import com.github.database.rider.springboot4.entity.user.Users;
import com.github.database.rider.springboot4.service.CompanyService;
import com.github.database.rider.springboot4.service.UsersService;

@DBRider //default datasource is used
@SpringBootTest
public class MultipleDataSourcesTest {

    @Autowired
    private UsersService usersService; //from user datasource

    @Autowired
    private CompanyService companyService; //from company datasource

    @Test
    @DataSet("users.yml")
    @Transactional
    public void shouldListUsers() {
        assertThat(this.usersService.count()).isEqualTo(3);
        assertThat(this.usersService.findByEmail("springboot@gmail.com").getId()).isEqualTo(3);
    }

    @Test
    @DBRider(dataSourceBeanName = "companyDataSource") //secondary (company) datasource will be used
    @DataSet("companies.yml")
    @Transactional
    public void shouldListCompanies() {
        assertThat(this.companyService.count()).isEqualTo(2);
        assertThat(this.companyService.findByNameLike("Umbrella%").getId()).isEqualTo(2);

    }

    @Test
    @DBRider(dataSourceBeanName = "companyDataSource")
    @DataSet(value = "companies.yml", cleanBefore = true)
    @ExpectedDataSet("expectedCompany.yml")
    @Transactional
    public void shouldDeleteCompany() {
    	this.companyService.deleteById(1L);
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet("expectedUsers.yml")
    @Transactional
    public void shouldDeleteUser() {
        assertThat(this.usersService.count()).isEqualTo(3);
        this.usersService.findById(2L).ifPresent(this.usersService::delete);
        //assertThat(userRepository.count()).isEqualTo(2); //assertion is made by @ExpectedDataset
    }

    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @DBRider(dataSourceBeanName = "companyDataSource")
    @ExpectedDataSet(value = "expectedCompanies.yml", compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    @Transactional
    public void shouldInsertCompanies() {
        assertThat(this.companyService.count()).isEqualTo(0);
        this.companyService.save(new Company("A corp"));
        this.companyService.save(new Company("Another corp"));
        this.companyService.save(new Company("DBRider corp"));
        this.companyService.save(new Company("Umbrella corporation"));
        assertThat(this.companyService.count()).isEqualTo(4);
    }

    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @ExpectedDataSet("user.yml")
    @Transactional
    public void shouldInsertUser() {
        assertThat(this.usersService).isNotNull();
        assertThat(this.usersService.count()).isEqualTo(0);
        this.usersService.save(new Users("newUser@gmail.com", "new user"));
        //assertThat(userRepository.count()).isEqualTo(1); //assertion is made by @ExpectedDataset
    }

}
