package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseLifecycleHooks extends ParentBaseLifecycleHooks {

    @BeforeAll
    @DataSet(value = "usersAndTweetsBeforeAllOnSuperClass.yml", disableConstraints = true, strategy = SeedStrategy.INSERT,
        replacers = JUnit5LifecycleHooksInSuperclassIt.UnixTimestampReplacer.class)
    static void baseLoadDataSetBeforeAll() {}

    @BeforeEach
    @DataSet(value = "tweetBeforeEachOnSuperClass.yml", disableConstraints = true, strategy = SeedStrategy.INSERT,
        replacers = JUnit5LifecycleHooksInSuperclassIt.UnixTimestampReplacer.class)
    void baseLoadDataSetBeforeEach() {}

}
