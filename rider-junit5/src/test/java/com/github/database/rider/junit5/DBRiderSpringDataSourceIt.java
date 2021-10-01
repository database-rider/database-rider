package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.incubating.Rider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitPlatform.class)
@SpringBootTest
@ContextConfiguration(classes = {DBRiderSpringDataSourceIt.TestConfig.class})
public class DBRiderSpringDataSourceIt {

    @Autowired
    @Qualifier("data-source-1")
    private DataSource dataSource1;

    @Autowired
    @Qualifier("data-source-2")
    private DataSource dataSource2;

    @Autowired
    private DataSource dataSourcePrimary;

    private JdbcTemplate jdbcTemplate1;
    private JdbcTemplate jdbcTemplate2;
    private JdbcTemplate jdbcTemplatePrimary;

    @BeforeEach
    public void setupJdbcTemplate() {
        jdbcTemplate1 = new JdbcTemplate(dataSource1);
        jdbcTemplate2 = new JdbcTemplate(dataSource2);
        jdbcTemplatePrimary = new JdbcTemplate(dataSourcePrimary);
    }

    @Rider(dataSourceBeanName = "data-source-1")
    @DataSet(value = "entity-ds1-test.yml")
    public void shouldPopulateDataSource1() {
        Set<String> expected = new HashSet<>(Arrays.asList("value1", "value2"));
        Set<String> actual = new HashSet<>(jdbcTemplate1.queryForList("SELECT value FROM Entity1", String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Rider(dataSourceBeanName = "data-source-2")
    @DataSet(value = "entity-ds2-test.yml")
    @Disabled("Spring users must use the Deprecated @DBRider Annotation here")
    public void shouldPopulateDataSource2() {
        Set<String> expected = new HashSet<>(Arrays.asList("value1", "value2"));
        Set<String> actual = new HashSet<>(jdbcTemplate2.queryForList("SELECT value FROM Entity2", String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Rider
    @DataSet(value = "entity-ds1-test.yml", executorId = "primary-ds-executor")
    public void shouldPopulateDataSourcePrimary() {
        Set<String> expected = new HashSet<>(Arrays.asList("value1", "value2"));
        Set<String> actual = new HashSet<>(jdbcTemplatePrimary.queryForList("SELECT value FROM Entity1", String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Configuration
    public static class TestConfig {

        @Bean(name = "data-source-1")
        @Primary
        public DataSource dataSourcePrimary() {
            return new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.HSQL)
                    .setScriptEncoding("UTF-8")
                    .addScript("scripts/schema1.sql")
                    .build();
        }

        @Bean(name = "data-source-2")
        public DataSource dataSourceSecondary() {
            return new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.HSQL)
                    .setScriptEncoding("UTF-8")
                    .addScript("scripts/schema2.sql")
                    .build();
        }
    }

}
