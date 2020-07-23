package com.github.database.rider.spring;

import com.github.database.rider.core.RiderRunner;
import com.github.database.rider.core.RiderTestContext;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.spring.api.DBRider;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * {@link org.springframework.test.context.TestExecutionListener TestExecutionListener}
 * which provides support for enabling database rider tests
 *
 * @author Artemy Osipov
 */
public class DBRiderTestExecutionListener extends AbstractTestExecutionListener {

    private static final String RIDER_TEST_CONTEXT = "RIDER_TEST_CONTEXT";

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        RiderTestContext riderTestContext = SpringRiderTestContext.create(testContext);
        testContext.setAttribute(RIDER_TEST_CONTEXT, riderTestContext);
        RiderRunner riderRunner = new RiderRunner();
        riderRunner.setup(riderTestContext);
        riderRunner.runBeforeTest(riderTestContext);
        DBRider dbRiderAnnotation = testContext.getTestMethod().getAnnotation(DBRider.class);
        RiderDataSource.DBType contextDBType = riderTestContext.getDataSetExecutor().getRiderDataSource().getDBType();
        if (dbRiderAnnotation.dataBaseType() != RiderDataSource.DBType.UNKNOWN && dbRiderAnnotation.dataBaseType() != contextDBType) {
            throw new IllegalArgumentException(String.format("Expect %s database instead of %s database.", dbRiderAnnotation.dataBaseType(), contextDBType));
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        RiderTestContext riderTestContext = (RiderTestContext) testContext.getAttribute(RIDER_TEST_CONTEXT);
        RiderRunner riderRunner = new RiderRunner();

        try {
            riderRunner.runAfterTest(riderTestContext);
        } finally {
            riderRunner.teardown(riderTestContext);
            riderTestContext.getDataSetExecutor().getRiderDataSource().getConnection().close();
        }
    }
}