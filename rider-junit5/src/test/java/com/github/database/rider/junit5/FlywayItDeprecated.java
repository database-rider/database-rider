package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by rafael-pestano on 13/09/2016.
 */
@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
@DBUnit(url = "jdbc:hsqldb:mem:flyway;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa")
public class FlywayItDeprecated {

    private static Flyway flyway;

    //private ConnectionHolder connectionHolder = () -> flyway.getDataSource().getConnection(); //also works, uncomment this and comment @DBUnit annotation


    @BeforeAll
    public static void initDB() {
        flyway = new Flyway();
        flyway.setDataSource("jdbc:hsqldb:mem:flyway;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("filesystem:src/test/resources/migration");
        flyway.migrate();

    }

    @Test
    @DataSet(value = "users.yml", executorId = "flyway")
    public void shouldListUsers() throws SQLException {
        try (Statement stmt = flyway.getDataSource().getConnection().createStatement()) {
            ResultSet resultSet = stmt.executeQuery("select * from user u order by id");
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString(2)).isEqualTo("@realpestano");
        }
    }

    @Test
    @DataSet(cleanBefore = true, transactional = true, executorId = "flyway")
    @ExpectedDataSet(value = "usersInserted.yml")
    public void shouldInserUsers() throws SQLException {
        Connection connection = flyway.getDataSource().getConnection();
        //connection.setAutoCommit(false); //transactional=true
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        statement.addBatch("INSERT INTO User VALUES (1, 'user1')");
        statement.addBatch("INSERT INTO User VALUES (2, 'user2')");
        statement.addBatch("INSERT INTO User VALUES (3, 'user3')");
        statement.executeBatch();
        //connection.commit();
        //connection.setAutoCommit(false);
    }
}
