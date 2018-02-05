package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSetExecutor;

import java.lang.annotation.Annotation;

public abstract class AbstractRiderTestContext implements RiderTestContext {

    protected final DataSetExecutor executor;

    public AbstractRiderTestContext(DataSetExecutor executor) {
        this.executor = executor;
    }

    @Override
    public DataSetExecutor getDataSetExecutor() {
        return executor;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        T annotation = getMethodAnnotation(clazz);

        if (annotation == null) {
            annotation = getClassAnnotation(clazz);
        }

        return annotation;
    }
}
