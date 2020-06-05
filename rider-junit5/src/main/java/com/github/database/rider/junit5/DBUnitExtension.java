package com.github.database.rider.junit5;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.database.rider.core.RiderRunner;
import com.github.database.rider.core.RiderTestContext;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.util.EntityManagerProvider;

/**
 * Created by pestano on 27/08/16.
 */
public class DBUnitExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback,
        BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private static final Namespace namespace = Namespace.create(DBUnitExtension.class);
    private static final String JUNIT5_EXECUTOR = "junit5";
    private static final String EMPTY_STRING = "";

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        clearEntityManager();
        final String executorId = getExecutorId(extensionContext, null);
        ConnectionHolder connectionHolder = getTestConnection(extensionContext, executorId);
        DataSetExecutor dataSetExecutor =  DataSetExecutorImpl.instance(executorId, connectionHolder);
        DBUnitTestContext dbUnitTestContext = getTestContext(extensionContext);
        dbUnitTestContext.setExecutor(dataSetExecutor);
        RiderTestContext riderTestContext = new JUnit5RiderTestContext(dbUnitTestContext.getExecutor(), extensionContext);
        RiderRunner riderRunner = new RiderRunner();
        riderRunner.setup(riderTestContext);
        riderRunner.runBeforeTest(riderTestContext);
        DBUnitConfig dbUnitConfig = riderTestContext.getDataSetExecutor().getDBUnitConfig();
        if (dbUnitConfig.isLeakHunter()) {
            LeakHunter leakHunter = LeakHunterFactory.from(dataSetExecutor.getRiderDataSource(), extensionContext.getRequiredTestMethod().getName());
            leakHunter.measureConnectionsBeforeExecution();
            dbUnitTestContext.setLeakHunter(leakHunter);
        }
    }

    private String getExecutorId(final ExtensionContext extensionContext, DataSet dataSet) {
        Optional<DataSet> annDataSet;
        if(dataSet != null) {
            annDataSet = Optional.of(dataSet);
        } else {
            annDataSet = findDataSetAnnotation(extensionContext);
        }

        String dataSourceBeanName = getConfiguredDataSourceBeanName(extensionContext);
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

    private ConnectionHolder getTestConnection(ExtensionContext extensionContext, String executorId) {
        if (isSpringExtensionEnabled() && isSpringTestContextEnabled(extensionContext)) {
            return getConnectionFromSpringContext(extensionContext, executorId);
        } else {
            return getConnectionFromTestClass(extensionContext, executorId);
        }
    }

    private ConnectionHolder getConnectionFromSpringContext(ExtensionContext extensionContext, String executorId) {
        String configuredDataSourceBeanName = getConfiguredDataSourceBeanName(extensionContext);
        DataSource dataSource = getDataSource(extensionContext, configuredDataSourceBeanName);
        try {
            DataSetExecutor dataSetExecutor = DataSetExecutorImpl.getExecutorById(executorId);
            if (isCachedConnection(dataSetExecutor)) {
                return new ConnectionHolderImpl(dataSetExecutor.getRiderDataSource().getConnection());
            } else {
                return new ConnectionHolderImpl(dataSource.getConnection());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not get connection from DataSource.");
        }
    }

    private static DataSource getDataSource(ExtensionContext extensionContext, String beanName) {
        ApplicationContext context = SpringExtension.getApplicationContext(extensionContext);
        return beanName.isEmpty() ? context.getBean(DataSource.class) : context.getBean(beanName, DataSource.class);
    }

    private static String getConfiguredDataSourceBeanName(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent()) {
            Optional<DBRider> annotation = AnnotationUtils.findAnnotation(testMethod.get(), DBRider.class);
            if (!annotation.isPresent()) {
                annotation = AnnotationUtils.findAnnotation(extensionContext.getRequiredTestClass(), DBRider.class);
            }
            return annotation.map(DBRider::dataSourceBeanName).orElse(EMPTY_STRING);
        } else {
            return EMPTY_STRING;
        }
    }

    private ConnectionHolder getConnectionFromTestClass(ExtensionContext extensionContext, String executorId) {
        DataSetExecutor dataSetExecutor = DataSetExecutorImpl.getExecutorById(executorId);
        if (isCachedConnection(dataSetExecutor)) {
            try {
                return new ConnectionHolderImpl(dataSetExecutor.getRiderDataSource().getConnection());
            } catch (SQLException e) {
                //intentional, if cached connection is invalid we can get a new one from test class method
            }
        }
        Class<?> testClass = extensionContext.getRequiredTestClass();
        ConnectionHolder conn = findConnectionFromTestClass(extensionContext, testClass);
        return conn;
    }

    private ConnectionHolder findConnectionFromTestClass(ExtensionContext extensionContext, Class<?> testClass) {
        try {
            Optional<Field> fieldFound = Arrays.stream(testClass.getDeclaredFields()).
                    filter(f -> f.getType() == ConnectionHolder.class).
                    findFirst();

            if (fieldFound.isPresent()) {
                Field field = fieldFound.get();
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object testInstance = Modifier.isStatic(field.getModifiers()) ? null : extensionContext.getRequiredTestInstance();
                ConnectionHolder connectionHolder = (ConnectionHolder) field.get(testInstance);
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
                ConnectionHolder connectionHolder = (ConnectionHolder) method.invoke(extensionContext.getRequiredTestInstance());
                if (connectionHolder == null) {
                    throw new RuntimeException("ConnectionHolder not initialized correctly");
                }
                return connectionHolder;
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not get database connection for test " + testClass, e);
        }

        if (testClass.getSuperclass() != null) {
            return findConnectionFromTestClass(extensionContext, testClass.getSuperclass());
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

    private boolean isSpringExtensionEnabled() {
        return isOnClasspath("org.springframework.test.context.junit.jupiter.SpringExtension");
    }

    private boolean isSpringTestContextEnabled(ExtensionContext extensionContext) {
        if(!extensionContext.getTestClass().isPresent()) {
            return false;
        }
        Store springStore = extensionContext.getRoot().getStore(Namespace.create(SpringExtension.class));
        return springStore != null && springStore.get(extensionContext.getTestClass().get()) != null;
    }

    private boolean isCachedConnection(DataSetExecutor executor) {
        return executor != null && executor.getDBUnitConfig().isCacheConnection();
    }

    private Optional<Method> findCallbackMethod(Class testClass, Class callback) {

        return Stream.of(testClass
                .getMethods())
                .filter(m -> m.getAnnotation(callback) != null)
                .findFirst();
    }

    private Optional<Method> findSuperclassCallbackMethod(Class testClass, Class callback) {
        Class<?> testSuperclass = testClass.getSuperclass();
        if (testSuperclass != null) {
            return findCallbackMethod(testSuperclass, callback);
        }
        return Optional.empty();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Optional<Method> callbackMethod = findCallbackMethod(extensionContext.getTestClass().get(), BeforeEach.class);
            if (callbackMethod.isPresent()) {
                executeDataSetForCallback(extensionContext, BeforeEach.class, callbackMethod.get());
                executeExpectedDataSetForCallback(extensionContext, BeforeEach.class, callbackMethod.get());
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Optional<Method> callbackMethod = findCallbackMethod(extensionContext.getTestClass().get(), AfterEach.class);
            if (callbackMethod.isPresent()) {
                executeDataSetForCallback(extensionContext, AfterEach.class, callbackMethod.get());
                executeExpectedDataSetForCallback(extensionContext, AfterEach.class, callbackMethod.get());
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Optional<Method> callbackMethod = findCallbackMethod(extensionContext.getTestClass().get(), BeforeAll.class);
            if (callbackMethod.isPresent()) {
                executeDataSetForCallback(extensionContext, BeforeAll.class, callbackMethod.get());
                executeExpectedDataSetForCallback(extensionContext, BeforeAll.class, callbackMethod.get());
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getTestClass().isPresent()) {
            Optional<Method> callbackMethod = findCallbackMethod(extensionContext.getTestClass().get(), AfterAll.class);
            if (callbackMethod.isPresent()) {
                executeDataSetForCallback(extensionContext, AfterAll.class, callbackMethod.get());
                executeExpectedDataSetForCallback(extensionContext, AfterAll.class, callbackMethod.get());
            }
        }
    }

    private void executeDataSetForCallback(ExtensionContext extensionContext, Class callbackAnnotation, Method callbackMethod) {
        Class testClass = extensionContext.getTestClass().get();
        // get DataSet annotation, if any
        Optional<DataSet> dataSetAnnotation = AnnotationUtils.findAnnotation(callbackMethod, DataSet.class);
        if (!dataSetAnnotation.isPresent()) {
            Optional<Method> superclassCallbackMethod = findSuperclassCallbackMethod(testClass, callbackAnnotation);
            if (superclassCallbackMethod.isPresent()) {
                dataSetAnnotation = AnnotationUtils.findAnnotation(superclassCallbackMethod.get(), DataSet.class);
            }
        }
        if(dataSetAnnotation.isPresent()) {
            clearEntityManager();
            DBUnitConfig dbUnitConfig = resolveDbUnitConfig(callbackAnnotation, callbackMethod, testClass);
            DataSet dataSet;
            if (dbUnitConfig.isMergeDataSets()) {
                Optional<DataSet> classLevelDataSetAnnotation = AnnotationUtils.findAnnotation(testClass, DataSet.class);
                dataSet = resolveDataSet(dataSetAnnotation, classLevelDataSetAnnotation);
            } else {
                dataSet = dataSetAnnotation.get();
            }
            // Execute dataset
            final String executorId = getExecutorId(extensionContext, dataSet);
            ConnectionHolder connectionHolder = getTestConnection(extensionContext, executorId);
            DataSetExecutor dataSetExecutor = DataSetExecutorImpl.instance(executorId, connectionHolder, dbUnitConfig);
            dataSetExecutor.createDataSet(new DataSetConfig().from(dataSet));
        }
    }

    private void executeExpectedDataSetForCallback(ExtensionContext extensionContext, Class callbackAnnotation, Method callbackMethod) throws DatabaseUnitException {
        Class testClass = extensionContext.getTestClass().get();
        // get ExpectedDataSet annotation, if any
        Optional<ExpectedDataSet> expectedDataSetAnnotation = AnnotationUtils.findAnnotation(callbackMethod, ExpectedDataSet.class);
        if (!expectedDataSetAnnotation.isPresent()) {
            Optional<Method> superclassCallbackMethod = findSuperclassCallbackMethod(testClass, callbackAnnotation);
            if (superclassCallbackMethod.isPresent()) {
                expectedDataSetAnnotation = AnnotationUtils.findAnnotation(superclassCallbackMethod.get(), ExpectedDataSet.class);
            }
        }
        if (expectedDataSetAnnotation.isPresent()) {
            ExpectedDataSet expectedDataSet = expectedDataSetAnnotation.get();
            // Resolve DBUnit config from annotation or file
            DBUnitConfig dbUnitConfig = resolveDbUnitConfig(callbackAnnotation, callbackMethod, testClass);
            // Verify expected dataset
            final String executorId = getExecutorId(extensionContext, null);
            ConnectionHolder connectionHolder = getTestConnection(extensionContext, executorId);
            DataSetExecutor dataSetExecutor = DataSetExecutorImpl.instance(executorId, connectionHolder, dbUnitConfig);
            dataSetExecutor.compareCurrentDataSetWith(
                    new DataSetConfig(expectedDataSet.value()).disableConstraints(true).datasetProvider(expectedDataSet.provider()),
                    expectedDataSet.ignoreCols(),
                    expectedDataSet.replacers(),
                    expectedDataSet.orderBy(),
                    expectedDataSet.compareOperation());
        }
    }

    // Resolve DBUnit config from annotation or file
    private DBUnitConfig resolveDbUnitConfig(Class callbackAnnotation, Method callbackMethod, Class testClass) {
        Optional<DBUnit> dbUnitAnnotation = AnnotationUtils.findAnnotation(callbackMethod, DBUnit.class);
        if (!dbUnitAnnotation.isPresent()) {
            dbUnitAnnotation = AnnotationUtils.findAnnotation(testClass, DBUnit.class);
        }
        if (!dbUnitAnnotation.isPresent()) {
            Optional<Method> superclassCallbackMethod = findSuperclassCallbackMethod(testClass, callbackAnnotation);
            if (superclassCallbackMethod.isPresent()) {
                dbUnitAnnotation = AnnotationUtils.findAnnotation(superclassCallbackMethod.get(), DBUnit.class);
            }
        }
        if (!dbUnitAnnotation.isPresent() && testClass.getSuperclass() != null) {
            dbUnitAnnotation = AnnotationUtils.findAnnotation(testClass.getSuperclass(), DBUnit.class);
        }
        return dbUnitAnnotation.isPresent() ? DBUnitConfig.from(dbUnitAnnotation.get()) : DBUnitConfig.fromGlobalConfig();
    }

    // Resolve dataSet annotation, merging class and method annotations if needed
    private DataSet resolveDataSet(Optional<DataSet> methodLevelDataSet,
                                   Optional<DataSet> classLevelDataSet) {
        if (classLevelDataSet.isPresent()) {
            return com.github.database.rider.core.util.AnnotationUtils.mergeDataSetAnnotations(classLevelDataSet.get(), methodLevelDataSet.get());
        } else {
            return methodLevelDataSet.get();
        }
    }

    private void clearEntityManager() {
        if (EntityManagerProvider.isEntityManagerActive()) {
            EntityManagerProvider.em().clear();
        }
    }
}
