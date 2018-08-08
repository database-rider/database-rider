package com.github.database.rider.core;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.core.util.AnnotationUtils;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.Connection;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
public class DBUnitRule implements TestRule {

    private DataSetExecutor executor;

    private DBUnitRule() {
    }

    public static DBUnitRule instance() {
        DBUnitRule instance = new DBUnitRule();
        instance.executor = DataSetExecutorImpl.instance(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID, null);

        return instance;
    }

    public static DBUnitRule instance(Connection connection) {
        return instance(new ConnectionHolderImpl(connection));
    }

    public static DBUnitRule instance(String executorName, Connection connection) {
        return instance(executorName, new ConnectionHolderImpl(connection));
    }

    public static DBUnitRule instance(ConnectionHolder connectionHolder) {
        return instance(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID, connectionHolder);
    }

    public static DBUnitRule instance(String executorName, ConnectionHolder connectionHolder) {
        DBUnitRule instance = new DBUnitRule();
        instance.executor = DataSetExecutorImpl.instance(executorName, connectionHolder);

        return instance;
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        DataSet dataSet = AnnotationUtils.findAnnotation(description, DataSet.class);
        if (dataSet == null) {
            dataSet = AnnotationUtils.findAnnotation(description.getTestClass(),DataSet.class);
        }

        if (dataSet != null && !"".equals(dataSet.executorId().trim())) {
            executor = DataSetExecutorImpl.getExecutorById(dataSet.executorId());
        }

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                RiderTestContext riderTestContext = new JUnit4RiderTestContext(executor, description);
                RiderRunner riderRunner = new RiderRunner();
                riderRunner.setup(riderTestContext);

                DBUnitConfig dbUnitConfig = riderTestContext.getDataSetExecutor().getDBUnitConfig();

                try {
                    riderRunner.runBeforeTest(riderTestContext);

                    LeakHunter leakHunter = null;
                    if (dbUnitConfig.isLeakHunter()) {
                        leakHunter = LeakHunterFactory.from(riderTestContext.getDataSetExecutor().getRiderDataSource(), riderTestContext.getMethodName());
                        leakHunter.measureConnectionsBeforeExecution();
                    }

                    statement.evaluate();

                    if (dbUnitConfig.isLeakHunter() && leakHunter != null) {
                        leakHunter.checkConnectionsAfterExecution();
                    }

                    riderRunner.runAfterTest(riderTestContext);
                } finally {
                    riderRunner.teardown(riderTestContext);
                }
            }
        };
    }

    public DataSetExecutor getDataSetExecutor() {
        return executor;
    }
}