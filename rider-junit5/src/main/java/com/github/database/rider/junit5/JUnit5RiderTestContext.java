package com.github.database.rider.junit5;

import com.github.database.rider.core.AbstractRiderTestContext;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.junit5.util.EntityManagerProvider;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static com.github.database.rider.junit5.util.EntityManagerProvider.em;
import static com.github.database.rider.junit5.util.EntityManagerProvider.isEntityManagerActive;

public class JUnit5RiderTestContext extends AbstractRiderTestContext {

    private final ExtensionContext extensionContext;

    public JUnit5RiderTestContext(DataSetExecutor executor, ExtensionContext extensionContext) {
        super(executor);
        this.extensionContext = extensionContext;
    }

    @Override
    public String getMethodName() {
        return extensionContext.getTestMethod()
                .map(Method::getName)
                .orElse(null);
    }

    @Override
    public <T extends Annotation> T getMethodAnnotation(Class<T> clazz) {
        return AnnotationUtils.findAnnotation(extensionContext.getTestMethod(), clazz).orElse(null);
    }

    @Override
    public <T extends Annotation> T getClassAnnotation(Class<T> clazz) {
        return getClassAnnotation(extensionContext.getTestClass(), clazz);
    }

    private  <T extends Annotation> T getClassAnnotation(Optional<Class<?>> testClass, Class<T> clazz) {
        return AnnotationUtils.findAnnotation(testClass, clazz)
            .orElseGet(() -> AnnotationUtils.findAnnotation(testClass.filter(Class::isMemberClass), Nested.class)
                .map(nested -> getClassAnnotation(testClass.map(Class::getEnclosingClass), clazz))
                .orElse(null)
            );
    }

    @Override
    public void commit() throws SQLException {
        if (isEntityManagerActive() && em().getTransaction().isActive()) {
            em().getTransaction().commit();
        } else {
            Connection connection = executor.getRiderDataSource().getDBUnitConnection().getConnection();
            connection.commit();
            connection.setAutoCommit(false);
        }
    }

    @Override
    public void beginTransaction() throws SQLException {
        if (EntityManagerProvider.isEntityManagerActive()) {
            EntityManagerProvider.em().getTransaction().begin();
        } else {
            Connection connection = executor.getRiderDataSource().getDBUnitConnection().getConnection();
            connection.setAutoCommit(false);
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (EntityManagerProvider.isEntityManagerActive() && EntityManagerProvider.em().getTransaction().isActive()) {
            EntityManagerProvider.em().getTransaction().rollback();
        } else {
            Connection connection = executor.getRiderDataSource().getDBUnitConnection().getConnection();
            connection.rollback();
        }
    }

    @Override
    public void clearEntityManager() {
        if (EntityManagerProvider.isEntityManagerActive()) {
            EntityManagerProvider.em().clear();
        }
    }
}
