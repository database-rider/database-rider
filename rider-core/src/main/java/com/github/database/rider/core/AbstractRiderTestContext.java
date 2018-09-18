package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.util.AnnotationUtils;

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

        T classAnnotation = getClassAnnotation(clazz);
        T methodAnnotation = getMethodAnnotation(clazz);
        
        if (executor.getDBUnitConfig().isMergeDataSets() && clazz.isAssignableFrom(DataSet.class) && (classAnnotation != null && methodAnnotation != null)) {
            return (T) AnnotationUtils.mergeDataSetAnnotations((DataSet) classAnnotation, (DataSet) methodAnnotation);
        }
        if (methodAnnotation != null) {
           return methodAnnotation;
        }

        return classAnnotation;
    }
}
