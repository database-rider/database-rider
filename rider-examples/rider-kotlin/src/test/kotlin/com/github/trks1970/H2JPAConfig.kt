package com.github.trks1970
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.hibernate.cfg.AvailableSettings
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.Properties
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@EnableJpaRepositories
@EnableTransactionManagement
class H2JPAConfig {

    @Bean
    fun dataSource(): DataSource {
        val builder = EmbeddedDatabaseBuilder()
        return builder
            .setType(EmbeddedDatabaseType.H2)
            .setName("testdb;DATABASE_TO_UPPER=false")
            .build()
    }

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter()
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.jpaVendorAdapter = vendorAdapter
        factory.setPackagesToScan("com.github.trks1970")
        factory.persistenceUnitName = "test"
        factory.dataSource = dataSource()
        factory.setJpaProperties(hibernateProperties())
        return factory
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory?): PlatformTransactionManager {
        val txManager = JpaTransactionManager()
        txManager.entityManagerFactory = entityManagerFactory
        return txManager
    }

    private fun hibernateProperties(): Properties {
        val properties = Properties()
        properties[AvailableSettings.DIALECT] = org.hibernate.dialect.H2Dialect::class.qualifiedName
        properties["javax.persistence.schema-generation.database.action"] = "drop-and-create"
        properties[AvailableSettings.PHYSICAL_NAMING_STRATEGY] = CamelCaseToUnderscoresNamingStrategy::class.qualifiedName
        properties[AvailableSettings.IMPLICIT_NAMING_STRATEGY] = SpringImplicitNamingStrategy::class.qualifiedName
        return properties
    }
}
