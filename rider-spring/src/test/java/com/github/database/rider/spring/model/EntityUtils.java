package com.github.database.rider.spring.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Artemy Osipov
 */
@Component
public class EntityUtils {

    private static final String INSERT_QUERY = "INSERT INTO Entity (value) VALUES (?)";
    private static final String SELECT_QUERY = "SELECT value FROM Entity";

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public EntityUtils(DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void addValues(String... values) {
        for (String val : values) {
            jdbcTemplate.update(INSERT_QUERY, val);
        }
    }

    public void executeInTransaction(TransactionCallback<?> callback) {
        transactionTemplate.execute(callback);
    }

    public void assertValues(String... values) {
        Set<String> expected = new HashSet<>(Arrays.asList(values));
        Set<String> actual = new HashSet<>(jdbcTemplate.queryForList(SELECT_QUERY, String.class));

        assertThat(actual).containsExactlyElementsOf(expected);
    }
}
