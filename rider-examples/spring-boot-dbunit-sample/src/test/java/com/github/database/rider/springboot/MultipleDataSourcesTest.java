package com.github.database.rider.springboot;

import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.springboot.model.company.Company;
import com.github.database.rider.springboot.model.company.CompanyRepository;
import com.github.database.rider.springboot.model.user.User;
import com.github.database.rider.springboot.model.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@SpringBootTest
public class MultipleDataSourcesTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;


    @Test
    @DataSet("users.yml")
    public void shouldListUsers() {
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findByEmail("springboot@gmail.com")).isEqualTo(new User(3));
    }

    @Test
    @DBRider(dataSourceBeanName = "companyDataSource")
    @DataSet("companies.yml")
    public void shouldListCompanies() {
        assertThat(companyRepository.count()).isEqualTo(2);
        assertThat(companyRepository.findByNameLike("Umbrella%")).isEqualTo(new Company(2));

    }

    @Test
    @DBRider(dataSourceBeanName = "companyDataSource")
    @DataSet(value = "companies.yml",cleanBefore = true)
    @ExpectedDataSet("expectedCompany.yml")
    public void shouldDeleteCompany() {
        companyRepository.deleteById(1L);
    }

    @Test
    @DataSet("users.yml")
    @ExpectedDataSet("expectedUsers.yml")
    public void shouldDeleteUser() {
        assertThat(userRepository.count()).isEqualTo(3);
        userRepository.findById(2L).ifPresent(userRepository::delete);
        //assertThat(userRepository.count()).isEqualTo(2); //assertion is made by @ExpectedDataset
    }

    @Test
    @DataSet(cleanBefore = true)//as we didn't declared a dataset DBUnit wont clear the table
    @DBRider(dataSourceBeanName = "companyDataSource")
    @ExpectedDataSet(value = "expectedCompanies.yml", compareOperation = CompareOperation.CONTAINS, ignoreCols = "id")
    public void shouldInsertCompanies() {
        assertThat(companyRepository.count()).isEqualTo(0);
        companyRepository.save(new Company("A corp"));
        companyRepository.save(new Company("Another corp"));
        companyRepository.save(new Company("DBRider corp"));
        companyRepository.save(new Company("Umbrella corporation"));
        assertThat(companyRepository.count()).isEqualTo(4);
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
