package com.github.database.rider.spring;

import com.github.database.rider.core.AbstractRiderTestContext;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.context.TestContext;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.SQLException;

class SpringRiderTestContext extends AbstractRiderTestContext {

    private final TestContext testContext;

    static SpringRiderTestContext create(TestContext testContext) {
        return new SpringRiderTestContext(createDataSetExecutor(testContext), testContext);
    }

    private static DataSetExecutor createDataSetExecutor(TestContext testContext) {
        DataSource dataSource = wrapInTransactionAwareProxy(testContext.getApplicationContext().getBean(DataSource.class));
        DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(dataSource::getConnection);
        dataSetExecutor.clearRiderDataSource();

        return dataSetExecutor;
    }

    private static DataSource wrapInTransactionAwareProxy(DataSource dataSource) {
        if (dataSource instanceof TransactionAwareDataSourceProxy) {
            return dataSource;
        } else {
            return new TransactionAwareDataSourceProxy(dataSource);
        }
    }

    private SpringRiderTestContext(DataSetExecutor executor, TestContext testContext) {
        super(executor);
        this.testContext = testContext;
    }

    @Override
    public String getMethodName() {
        return testContext.getTestMethod().getName();
    }

    @Override
    public <T extends Annotation> T getMethodAnnotation(Class<T> clazz) {
        return AnnotatedElementUtils.findMergedAnnotation(testContext.getTestMethod(), clazz);
    }

    @Override
    public <T extends Annotation> T getClassAnnotation(Class<T> clazz) {
        return AnnotatedElementUtils.findMergedAnnotation(testContext.getTestClass(), clazz);
    }

    @Override
    public void commit() throws SQLException {
        //no-op, this is done by SpringTest runner
    }

    @Override
    public void beginTransaction() throws SQLException {
        //no-op, this is done by SpringTest runner
    }

    @Override
    public void rollback() throws SQLException {
        //no-op, this is done by SpringTest runner
    }

    @Override
    public void clearEntityManager() {
        //no-op, this is done by SpringTest runner
    }
}
