package com.github.database.rider.core;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.util.AnnotationUtils;

import org.junit.runner.Description;

import java.lang.annotation.Annotation;

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
}
