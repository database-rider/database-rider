package com.github.database.rider.junit5;

import com.github.database.rider.core.AbstractRiderTestContext;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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
        return AnnotationUtils.findAnnotation(extensionContext.getTestClass(), clazz).orElse(null);

    }
}
