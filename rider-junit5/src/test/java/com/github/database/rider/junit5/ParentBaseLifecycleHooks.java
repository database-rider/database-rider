package com.github.database.rider.junit5;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@DBUnit(url = "jdbc:hsqldb:mem:junit5;DB_CLOSE_DELAY=-1", driver = "org.hsqldb.jdbcDriver", user = "sa")
public abstract class ParentBaseLifecycleHooks {

    @BeforeAll
    @DataSet(value = "usersAndTweetsBeforeAllOnParentSuperClass.yml", disableConstraints = true)
    static void parentLoadDataSetBeforeAll() {}

    @BeforeEach
    @DataSet(value = "tweetBeforeEachOnParentSuperClass.yml", disableConstraints = true, strategy = SeedStrategy.INSERT)
    void parentLoadDataSetBeforeEach() {}

    @AfterEach
    @ExpectedDataSet(value = "expectedUsersAndTweetsSuperclass.yml", orderBy = "CONTENT")
    void parentVerifyInvariantsAfterEach() {}

    @AfterAll
    @DataSet(value = "usersAndTweetsAfterAll.yml", disableConstraints = true)
    static void parentLoadDataSetAfterAll() {}
}
