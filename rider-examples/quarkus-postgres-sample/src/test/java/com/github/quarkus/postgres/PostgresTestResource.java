package com.github.quarkus.postgres;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("postgres")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Override
    public Map<String, String> start() {

        db.start();

        return Collections.singletonMap(
                "%test.quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:" + db.getMappedPort(5432) + "/postgres"
        );
    }

    @Override
    public void stop() {
        db.stop();
    }
}
