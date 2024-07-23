package com.github.database.rider.junit5;

import com.github.database.rider.core.RiderRunner;
import com.github.database.rider.core.RiderTestContext;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.database.rider.junit5.jdbc.ConnectionManager.getConfiguredDataSourceBeanName;
import static com.github.database.rider.junit5.jdbc.ConnectionManager.getCallbackConnection;
import static com.github.database.rider.junit5.jdbc.ConnectionManager.getTestConnection;
import static com.github.database.rider.junit5.util.Constants.*;
import static java.lang.String.format;

/**
 * Created by pestano on 27/08/16.
 */
public class DBUnitExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback,
        BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private static final Logger LOG = LoggerFactory.getLogger(DBUnitExtension.class.getName());

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        EntityManagerProvider.clear();
        DBUnitTestContext dbUnitTestContext = getTestContext(extensionContext);
        final DataSetExecutor dataSetExecutor = dbUnitTestContext.getExecutor();
        final DBUnitConfig dbUnitConfig = resolveDbUnitConfig(Optional.empty(), extensionContext.getTestMethod(), extensionContext.getRequiredTestClass());
        dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        if (dbUnitConfig.isLeakHunter()) {
            try {
                LeakHunter leakHunter = LeakHunterFactory.from(dataSetExecutor.getRiderDataSource(), extensionContext.getRequiredTestMethod().getName(), dbUnitConfig.isCacheConnection());
                leakHunter.measureConnectionsBeforeExecution();
                dbUnitTestContext.setLeakHunter(leakHunter);
            } catch (SQLException e) {
                LOG.warn(format("Could not create leak hunter for test %s", extensionContext.getRequiredTestMethod().getName()), e);
            }
        }
        RiderTestContext riderTestContext = new JUnit5RiderTestContext(dbUnitTestContext.getExecutor(), extensionContext);
        RiderRunner riderRunner = new RiderRunner();
        riderRunner.setup(riderTestContext);
        riderRunner.runBeforeTest(riderTestContext);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        final DBUnitTestContext dbUnitTestContext = getTestContext(extensionContext);
        final DBUnitConfig dbUnitConfig = dbUnitTestContext.getExecutor().getDBUnitConfig();
        RiderTestContext riderTestContext = new JUnit5RiderTestContext(dbUnitTestContext.getExecutor(), extensionContext);
        RiderRunner riderRunner = new RiderRunner();
        try {
            riderRunner.runAfterTest(riderTestContext);
            if (dbUnitConfig != null && dbUnitConfig.isLeakHunter()) {
                LeakHunter leakHunter = dbUnitTestContext.getLeakHunter();
                leakHunter.checkConnectionsAfterExecution();
            }
        } finally {
            riderRunner.teardown(riderTestContext);
        }
    }

    /**
     * one test context (datasetExecutor and dbunitConfig) per test
     */
    private DBUnitTestContext getTestContext(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Store store = context.getStore(NAMESPACE);
        return store.getOrComputeIfAbsent(testClass, (tc) -> createDBUnitTestContext(context), DBUnitTestContext.class);
    }

    private DBUnitTestContext createDBUnitTestContext(ExtensionContext extensionContext) {
        final String executorId = getExecutorId(extensionContext, null);
        final ConnectionHolder connectionHolder = getTestConnection(extensionContext, executorId);
        final DataSetExecutor dataSetExecutor = DataSetExecutorImpl.instance(executorId, connectionHolder);
        return new DBUnitTestContext(dataSetExecutor);
    }

    private Set<Method> findCallbackMethods(Class testClass, Class callback) {
        final Set<Method> methods = new HashSet<>();
        Stream.of(testClass.getSuperclass()
                        .getDeclaredMethods(), testClass.getDeclaredMethods())
                .flatMap(Stream::of)
                .filter(m -> m.getAnnotation(callback) != null)
                .forEach(m -> methods.add((Method) m)); //do not use Collectors.toSet here: stream incompatible types
        return Collections.unmodifiableSet(methods);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Set<Method> callbackMethods = findCallbackMethods(extensionContext.getTestClass().get(), BeforeEach.class);
            if (!callbackMethods.isEmpty()) {
                for (Method callbackMethod : callbackMethods) {
                    executeDataSetForCallback(extensionContext, BeforeEach.class, callbackMethod);
                    executeExpectedDataSetForCallback(extensionContext, BeforeEach.class, callbackMethod);
                }
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Set<Method> callbackMethods = findCallbackMethods(extensionContext.getTestClass().get(), AfterEach.class);
            if (!callbackMethods.isEmpty()) {
                for (Method callbackMethod : callbackMethods) {
                    executeDataSetForCallback(extensionContext, AfterEach.class, callbackMethod);
                    executeExpectedDataSetForCallback(extensionContext, AfterEach.class, callbackMethod);
                }
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Set<Method> callbackMethods = findCallbackMethods(extensionContext.getTestClass().get(), BeforeAll.class);
            if (!callbackMethods.isEmpty()) {
                for (Method callbackMethod : callbackMethods) {
                    executeDataSetForCallback(extensionContext, BeforeAll.class, callbackMethod);
                    executeExpectedDataSetForCallback(extensionContext, BeforeAll.class, callbackMethod);
                }
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Set<Method> callbackMethods = findCallbackMethods(extensionContext.getTestClass().get(), AfterAll.class);
            if (!callbackMethods.isEmpty()) {
                for (Method callbackMethod : callbackMethods) {
                    executeDataSetForCallback(extensionContext, AfterAll.class, callbackMethod);
                    executeExpectedDataSetForCallback(extensionContext, AfterAll.class, callbackMethod);
                }
            }
        }
    }

    private void executeDataSetForCallback(ExtensionContext extensionContext, Class callbackAnnotation, Method callbackMethod) throws SQLException {
        Class testClass = extensionContext.getTestClass().get();
        // get DataSet annotation, if any
        Optional<DataSet> dataSetAnnotation = AnnotationUtils.findAnnotation(callbackMethod, DataSet.class);
        if (!dataSetAnnotation.isPresent()) {
            LOG.warn("Could not find dataset annotation from callback method: " + callbackMethod);
            return;
        }
        EntityManagerProvider.clear();
        final DBUnitConfig dbUnitConfig = resolveDbUnitConfig(Optional.of(callbackAnnotation), Optional.of(callbackMethod), testClass);
        DataSet dataSet;
        if (dbUnitConfig.isMergeDataSets()) {
            Optional<DataSet> classLevelDataSetAnnotation = AnnotationUtils.findAnnotation(testClass, DataSet.class);
            dataSet = resolveDataSet(dataSetAnnotation, classLevelDataSetAnnotation, dbUnitConfig);
        } else {
            dataSet = dataSetAnnotation.get();
        }
        DataSetExecutor dataSetExecutor = getDataSetExecutor(extensionContext, callbackMethod, dataSet);
        dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        dataSetExecutor = resetExecutorConnectionIfNeeded(extensionContext, callbackAnnotation, dbUnitConfig, dataSetExecutor);
        dataSetExecutor.createDataSet(new DataSetConfig().from(dataSet));
        closeConnectionForAfterCallback(dataSetExecutor, callbackAnnotation);
    }

    private DataSetExecutor getDataSetExecutor(ExtensionContext extensionContext, Method callbackMethod, DataSet dataSet) {
        final String dataSourceBeanName = getConfiguredDataSourceBeanName(extensionContext, callbackMethod);
        final String executorId = getExecutorId(dataSourceBeanName, Optional.ofNullable(dataSet));
        final ConnectionHolder connectionHolder = getCallbackConnection(extensionContext, executorId, dataSourceBeanName);
        return DataSetExecutorImpl.instance(executorId, connectionHolder);
    }

    /**
     * We only need to close the connection in afterCallback because the connection opened in before callback is closed after test execution ({@link RiderRunner#teardown(RiderTestContext)})
     *
     * @param dataSetExecutor
     * @param callbackAnnotation
     * @throws SQLException
     */
    private void closeConnectionForAfterCallback(DataSetExecutor dataSetExecutor, Class callbackAnnotation) throws SQLException {
        if (!isAfterTestCallback(callbackAnnotation)) {
            return;
        }
        if (!dataSetExecutor.getDBUnitConfig().isCacheConnection() && !dataSetExecutor.getRiderDataSource().getDBUnitConnection().getConnection().isClosed()) {
            dataSetExecutor.getRiderDataSource().getDBUnitConnection().getConnection().close();
            ((DataSetExecutorImpl) dataSetExecutor).clearRiderDataSource();
        }
    }

    private void executeExpectedDataSetForCallback(ExtensionContext extensionContext, Class callbackAnnotation, Method callbackMethod) throws DatabaseUnitException, SQLException {
        Class testClass = extensionContext.getTestClass().get();
        // get ExpectedDataSet annotation, if any
        Optional<ExpectedDataSet> expectedDataSetAnnotation = AnnotationUtils.findAnnotation(callbackMethod, ExpectedDataSet.class);
        if (!expectedDataSetAnnotation.isPresent()) {
            LOG.warn("Could not find expectedDataSet annotation annotation from callback method: " + callbackMethod);
            return;
        }
        ExpectedDataSet expectedDataSet = expectedDataSetAnnotation.get();
        // Verify expected dataset
        // Resolve DBUnit config from annotation or file
        DBUnitConfig dbUnitConfig = resolveDbUnitConfig(Optional.of(callbackAnnotation), Optional.of(callbackMethod), testClass);
        DataSetExecutor dataSetExecutor = getDataSetExecutor(extensionContext, callbackMethod, null);
        dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        dataSetExecutor = resetExecutorConnectionIfNeeded(extensionContext, callbackAnnotation, dbUnitConfig, dataSetExecutor);
        dataSetExecutor.compareCurrentDataSetWith(
                new DataSetConfig(expectedDataSet.value()).disableConstraints(true).datasetProvider(expectedDataSet.provider()),
                expectedDataSet.ignoreCols(),
                expectedDataSet.replacers(),
                expectedDataSet.orderBy(),
                expectedDataSet.compareOperation());
        closeConnectionForAfterCallback(dataSetExecutor, callbackAnnotation);
    }

    private DataSetExecutor resetExecutorConnectionIfNeeded(ExtensionContext extensionContext, Class callbackAnnotation, DBUnitConfig dbUnitConfig, DataSetExecutor dataSetExecutor) {
        if (!dbUnitConfig.isCacheConnection() && isAfterTestCallback(callbackAnnotation)) { //we close the connection after test execution when cache is disabled so we need a new one for the callback
            final ConnectionHolder connectionHolder = getTestConnection(extensionContext, dataSetExecutor.getExecutorId());
            dataSetExecutor = DataSetExecutorImpl.instance(dataSetExecutor.getExecutorId(), connectionHolder, dbUnitConfig);
        }
        return dataSetExecutor;
    }

    private boolean isAfterTestCallback(Class callbackAnnotation) {
        return callbackAnnotation.equals(AfterEach.class) || callbackAnnotation.equals(AfterAll.class);
    }

    // Resolve DBUnit config from annotation or file
    private DBUnitConfig resolveDbUnitConfig(Optional<Class> callbackAnnotation, Optional<Method> method, Class testClass) {
        Optional<DBUnit> dbUnitAnnotation = AnnotationUtils.findAnnotation(method, DBUnit.class);
        if (!dbUnitAnnotation.isPresent()) {
            dbUnitAnnotation = AnnotationUtils.findAnnotation(testClass, DBUnit.class);
        }
        if (!dbUnitAnnotation.isPresent() && callbackAnnotation.isPresent()) {
            Set<Method> callbackMethods = findCallbackMethods(testClass, callbackAnnotation.get());
            if (!callbackMethods.isEmpty()) {
                dbUnitAnnotation = AnnotationUtils.findAnnotation(callbackMethods.iterator().next(), DBUnit.class);
            }
        }
        if (!dbUnitAnnotation.isPresent() && testClass.getSuperclass() != null) {
            dbUnitAnnotation = AnnotationUtils.findAnnotation(testClass.getSuperclass(), DBUnit.class);
        }
        return dbUnitAnnotation.isPresent() ? DBUnitConfig.from(dbUnitAnnotation.get()) : DBUnitConfig.fromGlobalConfig();
    }

    // Resolve dataSet annotation, merging class and method annotations if needed
    private DataSet resolveDataSet(Optional<DataSet> methodLevelDataSet,
                                   Optional<DataSet> classLevelDataSet, DBUnitConfig config) {
        if (classLevelDataSet.isPresent()) {
            if (DataSetMergingStrategy.METHOD.equals(config.getMergingStrategy())) {
                return com.github.database.rider.core.util.AnnotationUtils.mergeDataSetAnnotations(classLevelDataSet.get(), methodLevelDataSet.get());
            } else {
                return com.github.database.rider.core.util.AnnotationUtils.mergeDataSetAnnotations(methodLevelDataSet.get(), classLevelDataSet.get());
            }
        } else {
            return methodLevelDataSet.get();
        }
    }

    private String getExecutorId(final ExtensionContext extensionContext, DataSet dataSet) {
        Optional<DataSet> annDataSet;
        if (dataSet != null) {
            annDataSet = Optional.of(dataSet);
        } else {
            annDataSet = findDataSetAnnotation(extensionContext);
        }
        String dataSourceBeanName = getConfiguredDataSourceBeanName(extensionContext);
        return getExecutorId(dataSourceBeanName, annDataSet);
    }

    private String getExecutorId(String dataSourceBeanName, Optional<DataSet> annDataSet) {
        String executionIdSuffix = dataSourceBeanName.isEmpty() ? EMPTY_STRING : "-" + dataSourceBeanName;
        return annDataSet
                .map(DataSet::executorId)
                .filter(StringUtils::isNotBlank)
                .map(id -> id + executionIdSuffix)
                .orElseGet(() -> JUNIT5_EXECUTOR + executionIdSuffix);
    }

    private Optional<DataSet> findDataSetAnnotation(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent()) {
            Optional<DataSet> annDataSet = AnnotationUtils.findAnnotation(testMethod.get(), DataSet.class);
            if (!annDataSet.isPresent()) {
                annDataSet = AnnotationUtils.findAnnotation(extensionContext.getRequiredTestClass(), DataSet.class);
            }
            return annDataSet;
        } else {
            return Optional.empty();
        }
    }
}
