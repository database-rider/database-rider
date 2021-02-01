package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.configuration.DBUnitConfig;
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

        final DBUnitConfig config = executor.getDBUnitConfig();
        if (executor.getDBUnitConfig().isMergeDataSets() && clazz.isAssignableFrom(DataSet.class) && (classAnnotation != null && methodAnnotation != null)) {
            if(DataSetMergingStrategy.METHOD.equals(config.getMergingStrategy())) {
                return (T) AnnotationUtils.mergeDataSetAnnotations((DataSet) classAnnotation, (DataSet) methodAnnotation);
            } else {
                return (T) AnnotationUtils.mergeDataSetAnnotations((DataSet) methodAnnotation, (DataSet) classAnnotation);
            }
        }
        if (methodAnnotation != null) {
           return methodAnnotation;
        }

        return classAnnotation;
    }
}
