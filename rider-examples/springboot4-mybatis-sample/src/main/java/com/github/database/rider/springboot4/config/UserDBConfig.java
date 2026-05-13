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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;


@Configuration
@MapperScan(basePackages = "com.github.database.rider.springboot4.mapper.user", sqlSessionTemplateRef = "userSqlSessionTemplate")
public class UserDBConfig {

	@Primary
    @Bean(name = "userDataSourceProperties")
    @ConfigurationProperties("user.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "userDataSource")
    @ConfigurationProperties("user.datasource.configuration")
    DataSource dataSource(@Qualifier("userDataSourceProperties") DataSourceProperties userDataSourceProperties) {
        return userDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
	@Bean(name = "userSqlSessionFactory")
	SqlSessionFactory sqlSessionFactory(@Qualifier("userDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);

		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
		factoryBean.setMapperLocations(resolver.getResources("classpath:com/github/database/rider/springboot4/mapper/user/*.xml"));

		return factoryBean.getObject();
	}

    @Primary
	@Bean(name = "userSqlSessionTemplate")
	SqlSessionTemplate userSqlSessionTemplate(@Qualifier("userSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

    @Primary
	@Bean(name = "userDataSourceTransactionManager")
	DataSourceTransactionManager transactionManager(@Qualifier("userDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
