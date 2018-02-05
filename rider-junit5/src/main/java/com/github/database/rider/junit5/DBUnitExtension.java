package com.github.database.rider.junit5;

import com.github.database.rider.core.RiderRunner;
import com.github.database.rider.core.RiderTestContext;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by pestano on 27/08/16.
 */
public class DBUnitExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Namespace namespace = Namespace.create(DBUnitExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        ConnectionHolder connectionHolder = findTestConnection(extensionContext);

        if (EntityManagerProvider.isEntityManagerActive()) {
            EntityManagerProvider.em().clear();
        }

        DataSet dataSet = extensionContext.getRequiredTestMethod().getAnnotation(DataSet.class);
        if (dataSet == null) {
            dataSet = extensionContext.getRequiredTestClass().getAnnotation(DataSet.class);
        }

        DataSetExecutor executor;
        if (dataSet == null) {
            executor = DataSetExecutorImpl.instance(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID, connectionHolder);
        } else {
            executor = DataSetExecutorImpl.instance(dataSet.executorId(), connectionHolder);
        }

        DBUnitTestContext dbUnitTestContext = getTestContext(extensionContext);
        dbUnitTestContext.setExecutor(executor);

        RiderTestContext riderTestContext = new JUnit5RiderTestContext(dbUnitTestContext.getExecutor(), extensionContext);

        RiderRunner riderRunner = new RiderRunner();
        riderRunner.setup(riderTestContext);
        riderRunner.runBeforeTest(riderTestContext);

        DBUnitConfig dbUnitConfig = riderTestContext.getDataSetExecutor().getDBUnitConfig();

        if (dbUnitConfig.isLeakHunter()) {
            LeakHunter leakHunter = LeakHunterFactory.from(executor.getRiderDataSource(), extensionContext.getRequiredTestMethod().getName());
            leakHunter.measureConnectionsBeforeExecution();
            dbUnitTestContext.setLeakHunter(leakHunter);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        DBUnitTestContext dbUnitTestContext = getTestContext(extensionContext);
        DBUnitConfig dbUnitConfig = dbUnitTestContext.getExecutor().getDBUnitConfig();

        RiderTestContext riderTestContext = new JUnit5RiderTestContext(dbUnitTestContext.getExecutor(), extensionContext);
        RiderRunner riderRunner = new RiderRunner();

        try {
            if (dbUnitConfig != null && dbUnitConfig.isLeakHunter()) {
                LeakHunter leakHunter = dbUnitTestContext.getLeakHunter();
                leakHunter.checkConnectionsAfterExecution();
            }

            riderRunner.runAfterTest(riderTestContext);
        } finally {
            riderRunner.teardown(riderTestContext);
        }
    }

    private ConnectionHolder findTestConnection(ExtensionContext extensionContext) {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        try {
            Optional<Field> fieldFound = Arrays.stream(testClass.getDeclaredFields()).
                    filter(f -> f.getType() == ConnectionHolder.class).
                    findFirst();

            if (fieldFound.isPresent()) {
                Field field = fieldFound.get();
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                ConnectionHolder connectionHolder = ConnectionHolder.class.cast(field.get(extensionContext.getRequiredTestInstance()));
                if (connectionHolder == null) {
                    throw new RuntimeException("ConnectionHolder not initialized correctly");
                }
                return connectionHolder;
            }

            //try to get connection from method

            Optional<Method> methodFound = Arrays.stream(testClass.getDeclaredMethods()).
                    filter(m -> m.getReturnType() == ConnectionHolder.class).
                    findFirst();

            if (methodFound.isPresent()) {
                Method method = methodFound.get();
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                ConnectionHolder connectionHolder = ConnectionHolder.class.cast(method.invoke(extensionContext.getRequiredTestInstance()));
                if (connectionHolder == null) {
                    throw new RuntimeException("ConnectionHolder not initialized correctly");
                }
                return connectionHolder;
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not get database connection for test " + testClass, e);
        }

        return null;
    }

    /**
     * one test context (datasetExecutor, dbunitConfig etc..) per test
     */
    private DBUnitTestContext getTestContext(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Store store = context.getStore(namespace);

        return store.getOrComputeIfAbsent(testClass, (tc) -> new DBUnitTestContext(), DBUnitTestContext.class);
    }
}
