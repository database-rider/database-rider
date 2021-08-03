package com.github.database.rider.core.util;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.connection.RiderDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
@DBUnit(cacheTableNames = false)
public class TableNameResolverTest {

    @Test
    public void shouldGetTableNames() {
        TableNameResolver tableNameResolver = new TableNameResolver();
        Set<String> tableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("rider-it").connection())));
        assertThat(tableNames).hasSize(4)
                .containsExactly("PUBLIC.\"USER\"", "PUBLIC.FOLLOWER", "PUBLIC.SEQUENCE", "PUBLIC.TWEET");
    }

    @Test
    public void shouldCacheTableNames() {
        TableNameResolver tableNameResolver = new TableNameResolver();

        Set<String> tableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("rider-it").connection())));
        assertThat(tableNames).hasSize(4)
                .containsExactly("PUBLIC.\"USER\"", "PUBLIC.FOLLOWER", "PUBLIC.SEQUENCE", "PUBLIC.TWEET");

        Set<String> newTableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("escape-pattern").connection())));

        assertThat(newTableNames).isEqualTo(tableNames);
    }

    @Test
    public void shouldNotCacheTableNames() {
        TableNameResolver tableNameResolver = new TableNameResolver(DBUnitConfig.from(TableNameResolverTest.class.getAnnotation(DBUnit.class)));

        Set<String> tableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("rider-it").connection())));
        assertThat(tableNames).hasSize(4)
                .containsExactly("PUBLIC.\"USER\"", "PUBLIC.FOLLOWER", "PUBLIC.SEQUENCE", "PUBLIC.TWEET");

        Set<String> newTableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("escape-pattern").connection())));

        assertThat(newTableNames).isNotEqualTo(tableNames)
                .containsExactly("PUBLIC.ORDER", "PUBLIC.SEQUENCE");
    }

    @DBUnit(escapePattern = "***?***")
    @Test
    public void shouldEscapeTableName() throws NoSuchMethodException {
        TableNameResolver tableNameResolver = new TableNameResolver(DBUnitConfig.from(TableNameResolverTest.class.getMethod("shouldEscapeTableName")));
        Set<String> tableNames = tableNameResolver.getTableNames(new RiderDataSource(
                new ConnectionHolderImpl(EntityManagerProvider.instance("escape-pattern").connection())));

        assertThat(tableNames).hasSize(2)
                .containsExactlyInAnyOrder("PUBLIC.***SEQUENCE***", "PUBLIC.***ORDER***");
    }

}
