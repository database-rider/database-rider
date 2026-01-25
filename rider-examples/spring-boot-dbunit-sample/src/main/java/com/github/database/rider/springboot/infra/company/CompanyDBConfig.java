package com.github.database.rider.springboot.infra.company;

import com.github.database.rider.springboot.model.company.CompanyRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import jakarta.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "companyEntityManagerFactory", basePackages = {
        "com.github.database.rider.springboot.model.company", "com.github.database.rider.springboot.infra.company"},
        transactionManagerRef = "companyTransactionManager")
public class CompanyDBConfig {

    @Autowired
    private Environment env;

    @Bean(name = "companyDataSourceProperties")
    @ConfigurationProperties("company.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "companyDataSource")
    @ConfigurationProperties("company.datasource.configuration")
    public DataSource dataSource(@Qualifier("companyDataSourceProperties") DataSourceProperties companyDataSourceProperties) {
        return companyDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "companyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("companyDataSource") DataSource companyDataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",
                env.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect",
                env.getProperty("spring.jpa.properties.hibernate.dialect"));
        return builder
                .dataSource(companyDataSource)
                .properties(properties)
                .packages(CompanyRepository.class.getPackage().getName())
                .persistenceUnit("companyPU")
                .build();
    }

    @Bean(name = "companyTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("companyEntityManagerFactory") EntityManagerFactory companyEntityManagerFactory) {
        return new JpaTransactionManager(companyEntityManagerFactory);
    }

}
