package com.github.database.rider.junit5.integration;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.jdbc.ConnectionManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;
import static com.github.database.rider.junit5.jdbc.ConnectionManager.getConnectionHolder;

public class Spring {

    public static boolean isEnabled(ExtensionContext extensionContext) {
        if (!extensionContext.getTestClass().isPresent()) {
            return false;
        }
        if(!isSpringExtensionEnabled(extensionContext)) {
            return false;
        }
        ExtensionContext.Store springStore = extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(SpringExtension.class));
        return springStore != null && springStore.get(extensionContext.getTestClass().get()) != null;
    }

    public static ConnectionHolder getConnectionFromSpringContext(ExtensionContext extensionContext, String executorId) {
        String configuredDataSourceBeanName = ConnectionManager.getConfiguredDataSourceBeanName(extensionContext);
        DataSource dataSource = getDataSourceFromSpringContext(extensionContext, configuredDataSourceBeanName);
        return getConnectionHolder(executorId, dataSource);
    }

    private static DataSource getDataSourceFromSpringContext(ExtensionContext extensionContext, String beanName) {
        ApplicationContext context = SpringExtension.getApplicationContext(extensionContext);
        return beanName.isEmpty() ? context.getBean(DataSource.class) : context.getBean(beanName, DataSource.class);
    }

    private static boolean isSpringExtensionEnabled(ExtensionContext extensionContext) {
        try {
            return isOnClasspath("org.springframework.test.context.junit.jupiter.SpringExtension") && extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(SpringExtension.class)) != null;
        } catch (Exception e) {
            return false;
        }
    }

}