package com.github.database.rider.springboot4.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = "com.github.database.rider.springboot4.mapper.company", sqlSessionTemplateRef = "companySqlSessionTemplate")
public class CompanyDBConfig {

    @Bean(name = "companyDataSourceProperties")
    @ConfigurationProperties("company.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "companyDataSource")
    @ConfigurationProperties("company.datasource.configuration")
    DataSource dataSource(@Qualifier("companyDataSourceProperties") DataSourceProperties companyDataSourceProperties) {
        return companyDataSourceProperties.initializeDataSourceBuilder().build();
    }

	@Bean(name = "companySqlSessionFactory")
	SqlSessionFactory mysqlSessionFactory(@Qualifier("companyDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);

		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
		factoryBean.setMapperLocations(resolver.getResources("classpath:com/github/database/rider/springboot4/mapper/company/*.xml"));

		return factoryBean.getObject();
	}

	@Bean(name = "companySqlSessionTemplate")
	SqlSessionTemplate userSqlSessionTemplate(@Qualifier("companySqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean(name = "companyDataSourceTransactionManager")
	DataSourceTransactionManager transactionManager(@Qualifier("companyDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
