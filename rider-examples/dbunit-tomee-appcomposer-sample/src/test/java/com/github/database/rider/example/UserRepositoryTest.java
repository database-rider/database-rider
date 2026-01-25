package com.github.database.rider.example;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.dataset.DataSet;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.testing.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.rules.RuleChain.outerRule;

@Default
@Classes(cdi = true)
@Descriptors(@Descriptor(name = "persistence.xml", path = "META-INF/persistence.xml"))
@ContainerProperties({
        @ContainerProperties.Property(name = "jdbc/user", value = "new://Resource?type=DataSource"),
        @ContainerProperties.Property(name = "jdbc/user.LogSql", value = "true")
})
@DataSet(cleanBefore = true)
public class UserRepositoryTest {

    @Resource
    private DataSource dataSource;

    @Rule
    public final TestRule rules = outerRule(new ApplicationComposerRule(this))
            .around(DBUnitRule.instance(() -> dataSource.getConnection())
            );


    @Inject
    private UserRepository repository;


    @Test
    @DataSet("datasets/users.yml")
    public void find1() {
        assertEquals("John Smith", repository.find(1L).getName());
        assertEquals("Clark Kent", repository.find(2L).getName());
    }

    @Test
    public void find2() { // ensure we didn't leak previous dataset
        assertNull(repository.find(1L));
    }
}
