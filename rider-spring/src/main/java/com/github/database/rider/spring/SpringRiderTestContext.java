package com.github.database.rider.spring;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.sql.DataSource;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.context.TestContext;

import com.github.database.rider.core.AbstractRiderTestContext;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.spring.api.DBRider;

class SpringRiderTestContext extends AbstractRiderTestContext {

    private static final String EMPTY_STRING = "";

    private final TestContext testContext;

    static SpringRiderTestContext create(TestContext testContext) {
        return new SpringRiderTestContext(createDataSetExecutor(testContext), testContext);
    }

    private static DataSetExecutor createDataSetExecutor(TestContext testContext) {
        String beanName = getConfiguredDataSourceBeanName(testContext);
        DataSource dataSourceBean = getDataSource(testContext, beanName);
        final DataSource dataSource = wrapInTransactionAwareProxy(dataSourceBean);
        String instanceId = beanName.isEmpty() ? "default" : beanName;
        DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(instanceId, new ConnectionHolder() {
            @Override
            public Connection getConnection() throws SQLException {
                return dataSource.getConnection();
            }
        });
        dataSetExecutor.clearRiderDataSource();

        return dataSetExecutor;
    }

    private static DataSource getDataSource(TestContext testContext, String beanName) {
        ApplicationContext context = testContext.getApplicationContext();
        return beanName.isEmpty() ? context.getBean(DataSource.class) : context.getBean(beanName, DataSource.class);
    }

    private static String getConfiguredDataSourceBeanName(final TestContext testContext) {
        DBRider dbRiderAnnotation = testContext.getTestMethod().getAnnotation(DBRider.class);
        if(dbRiderAnnotation == null) {
            dbRiderAnnotation = testContext.getTestClass().getAnnotation(DBRider.class);
        }
        return dbRiderAnnotation != null ? dbRiderAnnotation.dataSourceBeanName() : EMPTY_STRING;
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
