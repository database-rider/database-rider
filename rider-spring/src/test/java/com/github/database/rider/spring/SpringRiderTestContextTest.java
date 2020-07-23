package com.github.database.rider.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.spring.api.DBRider;
import com.github.database.rider.spring.config.TestConfig;

/**
 * @author kerraway
 * @date 2020/07/23
 * @see SpringRiderTestContext
 */
public class SpringRiderTestContextTest {
    @Test
    public void useDefaultDataSourceByClassConfiguration() throws Exception {
        createRiderTestContextAndAssertExecutorId("useDefaultDataSourceByClassConfiguration", "default");
    }

    @Test
    public void useDefaultDataSource() throws Exception {
        createRiderTestContextAndAssertExecutorId("useDefaultDataSource", "default");
    }

    @Test
    public void useDefaultDataSourceAndRequireHsqldb() throws Exception {
        createRiderTestContextAndAssertExecutorId("useDefaultDataSourceAndRequireHsqldb", "default");
    }

    @Test(expected = IllegalStateException.class)
    public void useDefaultDataSourceAndRequireH2() throws Exception {
        createRiderTestContextAndAssertExecutorId("useDefaultDataSourceAndRequireH2", null);
    }

    @Test
    public void useSecondDataSource() throws Exception {
        createRiderTestContextAndAssertExecutorId("useSecondDataSource", "data-source-2");
    }

    @Test
    public void useSecondDataSourceAndRequireHsqldb() throws Exception {
        createRiderTestContextAndAssertExecutorId("useSecondDataSourceAndRequireHsqldb", "data-source-2");
    }

    @Test(expected = IllegalStateException.class)
    public void useSecondDataSourceAndRequireH2() throws Exception {
        createRiderTestContextAndAssertExecutorId("useSecondDataSourceAndRequireH2", null);
    }

    private void createRiderTestContextAndAssertExecutorId(String methodName, String expectedExecutorId) throws Exception {
        TestContext testContext = getTestContext(new DbRiderUsage(), methodName);

        SpringRiderTestContext riderTestContext = SpringRiderTestContext.create(testContext);

        assertNotNull(riderTestContext);
        assertEquals(expectedExecutorId, riderTestContext.getDataSetExecutor().getExecutorId());
    }

    private TestContext getTestContext(Object instance, String methodName) throws Exception {
        TestContextManager testContextManager = new TestContextManager(instance.getClass());
        testContextManager.prepareTestInstance(instance);
        testContextManager.beforeTestMethod(instance, ClassUtils.getMethod(instance.getClass(), methodName));
        return testContextManager.getTestContext();
    }

    @DBRider
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = TestConfig.class)
    static class DbRiderUsage {
        public void useDefaultDataSourceByClassConfiguration() {
        }

        @DBRider
        public void useDefaultDataSource() {
        }

        @DBRider(databaseType = RiderDataSource.DBType.HSQLDB)
        public void useDefaultDataSourceAndRequireHsqldb() {
        }

        @DBRider(databaseType = RiderDataSource.DBType.H2)
        public void useDefaultDataSourceAndRequireH2() {
        }

        @DBRider(dataSourceBeanName = "data-source-2")
        public void useSecondDataSource() {
        }

        @DBRider(dataSourceBeanName = "data-source-2", databaseType = RiderDataSource.DBType.HSQLDB)
        public void useSecondDataSourceAndRequireHsqldb() {
        }

        @DBRider(dataSourceBeanName = "data-source-2", databaseType = RiderDataSource.DBType.H2)
        public void useSecondDataSourceAndRequireH2() {
        }
    }
}