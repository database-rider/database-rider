package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSetExecutor;

import java.lang.annotation.Annotation;
import java.sql.SQLException;

public interface RiderTestContext {

    DataSetExecutor getDataSetExecutor();
    String getMethodName();
    <T extends Annotation> T getAnnotation(Class<T> clazz);
    <T extends Annotation> T getMethodAnnotation(Class<T> clazz);
    <T extends Annotation> T getClassAnnotation(Class<T> clazz);

    void commit() throws SQLException;

    void beginTransaction() throws SQLException;

    void rollback() throws SQLException;

    void clearEntityManager();
}
