package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.util.AnnotationUtils;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

public class JUnit4RiderTestContext extends AbstractRiderTestContext {

    private final Description description;

    public JUnit4RiderTestContext(DataSetExecutor executor, Description description) {
        super(executor);
        this.description = description;
    }

    @Override
    public String getMethodName() {
        return description.getMethodName();
    }

    @Override
    public <T extends Annotation> T getMethodAnnotation(Class<T> clazz) {
        if (description.isTest()) {
            return AnnotationUtils.findAnnotation(description, clazz);
        }

        return null;
    }

    @Override
    public <T extends Annotation> T getClassAnnotation(Class<T> clazz) {
        return AnnotationUtils.findAnnotation(description.getTestClass(), clazz);
    }

    @Override
    public void commit() throws SQLException {
        if (isEntityManagerActive() && em().getTransaction().isActive()) {
            em().getTransaction().commit();
        } else {
            Connection connection = executor.getRiderDataSource().getConnection();
            connection.commit();
            connection.setAutoCommit(false);
        }
    }

    @Override
    public void beginTransaction() throws SQLException {
        if (isEntityManagerActive()) {
            em().getTransaction().begin();
        } else {
            Connection connection = executor.getRiderDataSource().getConnection();
            connection.setAutoCommit(false);
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (isEntityManagerActive() && em().getTransaction().isActive()) {
            em().getTransaction().rollback();
        } else {
            Connection connection = executor.getRiderDataSource().getConnection();
            connection.rollback();
        }
    }

    @Override
    public void clearEntityManager() {
        if (isEntityManagerActive()) {
            em().clear();
        }
    }


}
