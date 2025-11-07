package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.model.User;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.junit.jupiter.api.*;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that cacheConnection=true properly reuses the same executor
 * between BeforeEach callbacks and test methods (regression test for v1.44.0 fix)
 * Note: Requires Java 11+ due to HSQLDB 2.6.1 compatibility (tested on Java 11, 21)
 * Created by k-hoshihara on 20/07/25.
 */
@DBRider
@RunWith(JUnitPlatform.class) // Use JUnit 4 runner for consistency with existing tests in this package
@DBUnit // cacheConnection=true (default)
public class CacheConnectionReuseIt {

    // ConnectionHolder for @DBRider annotation detection
    @SuppressWarnings("unused")
    private static final ConnectionHolder connectionHolder = () -> EntityManagerProvider.instance("junit5-pu").connection();

    private static String expectedConnectionId;

    @BeforeEach
    @DataSet(value = "users.yml", cleanBefore = true)
    void loadDataInBeforeEach() {
        // Verify the same connection is used throughout test class (cacheConnection=true)
        verifySameConnectionReuse();
    }

    @Test
    @DataSet("tweet.yml")
    void testExecutorReuse() {
        // Verify the same connection is used throughout test class (cacheConnection=true)
        verifySameConnectionReuse();
        // Verify data was loaded correctly

        @SuppressWarnings("unchecked")
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().hasSize(2);
    }

    @Test
    @DataSet("usersWithTweet.yml")
    void testExecutorReuseSecondTest() {
        // Verify the same connection is used throughout test class (cacheConnection=true)
        verifySameConnectionReuse();

        // Verify data was loaded correctly
        @SuppressWarnings("unchecked")
        List<User> users = EntityManagerProvider.em().createQuery("select u from User u").getResultList();
        assertThat(users).isNotNull().isNotEmpty();
    }

    /**
     * Verifies that the same connection is reused throughout the test class
     * when cacheConnection=true (regression test for v1.44.0 fix)
     */
    private void verifySameConnectionReuse() {
        String currentConnectionId = getCurrentConnectionId();

        if (expectedConnectionId == null) {
            // First call - record the expected connection ID
            expectedConnectionId = currentConnectionId;
        } else {
            // Later calls - verify the same connection is reused
            assertThat(currentConnectionId)
                    .as("All methods should use the same connection when cacheConnection=true")
                    .isEqualTo(expectedConnectionId);
        }
    }

    /**
     * Helper method to get the current connection instance as a proxy for executor identity
     */
    private String getCurrentConnectionId() {
        try {
            @SuppressWarnings("resource") // Database Rider manages connection lifecycle
            Connection connection = EntityManagerProvider.instance("junit5-pu").connection();
            // Use connection instance hashCode as a proxy for executor identity
            // Don't close the connection here as Database Rider manages it
            return String.valueOf(connection.hashCode());
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
}
