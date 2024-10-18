package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@DBUnit(url = "jdbc:hsqldb:mem:junit5;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa")
public abstract class ParentBaseLifecycleHooks {

    @BeforeAll
    @DataSet(value = "usersAndTweetsBeforeAll.yml", disableConstraints = true)
    public static void loadDataSetBeforeAll() {
    }

    @BeforeEach
    @DataSet(value = "tweetBeforeEachOnSuperClass.yml", disableConstraints = true)
    public void loadDataSetBeforeEach() {
    }

    @AfterEach
    @ExpectedDataSet(value = "expectedTweetsAfterEachSuperclass.yml", orderBy = "CONTENT")
    public void verifyInvariantsAfterEach() {
    }

    @AfterAll
    @DataSet(value = "usersAndTweetsAfterAll.yml", disableConstraints = true)
    public static void loadDataSetAfterAll() {
    }

}
