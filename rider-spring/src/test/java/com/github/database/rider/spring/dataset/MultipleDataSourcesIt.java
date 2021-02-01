package com.github.database.rider.spring.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import com.github.database.rider.spring.config.TestConfig;

/**
 * Simple test to verify that DBRider annotation works with custom dataSourceBeanName.
 * Custom dataSource is autowired into test to create JdbcTemplate used to query the table.
 * A separate data set is used to setup the database by DBRider.
 */
@RunWith(SpringRunner.class)
@DBRider(dataSourceBeanName = "data-source-2")
@ContextConfiguration(classes = TestConfig.class)
public class MultipleDataSourcesIt {

    @Autowired
    @Qualifier("data-source-2")
    private DataSource secondaryDataSource;

    @Autowired
    private DataSource defaultDataSource;

    private JdbcTemplate jdbcTemplate;

    @Test
    @DataSet(value = "ds2-test.yml")
    public void shouldUseSecondaryDataSource() {
        jdbcTemplate = new JdbcTemplate(secondaryDataSource);
        Set<String> expected = new HashSet<>(Arrays.asList("value1", "value2"));
        Set<String> actual = new HashSet<>(jdbcTemplate.queryForList("SELECT value FROM Entity2", String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @DataSet(value = "test.yml")
    @DBRider
    public void shouldUseDefaultDataSource() {
        jdbcTemplate = new JdbcTemplate(defaultDataSource);
        Set<String> expected = new HashSet<>(Arrays.asList("value1", "value2"));
        Set<String> actual = new HashSet<>(jdbcTemplate.queryForList("SELECT value FROM Entity", String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }


}
