package com.github.database.rider.junit5.jdbc;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.integration.Micronaut;
import com.github.database.rider.junit5.integration.Spring;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static com.github.database.rider.junit5.util.Constants.EMPTY_STRING;

public final class ConnectionManager {

    public static ConnectionHolder getTestConnection(ExtensionContext extensionContext, String executorId) {
        if (Spring.isEnabled(extensionContext)) {
            return Spring.getConnectionFromSpringContext(extensionContext, executorId);
        } else if (Micronaut.isEnabled(extensionContext)) {
            return Micronaut.getConnectionFromMicronautContext(extensionContext, executorId);
        }
        return getConnectionFromTestClass(extensionContext, executorId);
    }

    public static String getConfiguredDataSourceBeanName(ExtensionContext extensionContext) {
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

    public static ConnectionHolder getConnectionHolder(String executorId, DataSource dataSource) {
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

    private static ConnectionHolder getConnectionFromTestClass(ExtensionContext extensionContext, String executorId) {
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

    private static ConnectionHolder findConnectionFromTestClass(ExtensionContext extensionContext, Class<?> testClass) {
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

    private static boolean isCachedConnection(DataSetExecutor executor) {
        return executor != null && executor.getDBUnitConfig().isCacheConnection();
    }

}