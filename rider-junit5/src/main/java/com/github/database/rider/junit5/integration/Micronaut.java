package com.github.database.rider.junit5.integration;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import jakarta.sql.DataSource;
import java.util.Optional;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;
import static com.github.database.rider.junit5.jdbc.ConnectionManager.getConfiguredDataSourceBeanName;
import static com.github.database.rider.junit5.jdbc.ConnectionManager.getConnectionHolder;

public class Micronaut {

    public static boolean isEnabled(ExtensionContext extensionContext) {
        return isExtensionEnabled(extensionContext) && getApplicationContext(extensionContext).isPresent();
    }

    public static ConnectionHolder getConnectionFromMicronautContext(ExtensionContext extensionContext, String executorId) {
        String configuredDataSourceBeanName = getConfiguredDataSourceBeanName(extensionContext);
        return getConnectionFromMicronautContext(extensionContext, executorId, configuredDataSourceBeanName);
    }

    public static ConnectionHolder getConnectionFromMicronautContext(ExtensionContext extensionContext, String executorId, String dataSourceBeanName) {
        DataSource dataSource = getDataSourceFromMicronautContext(extensionContext, dataSourceBeanName);
        return getConnectionHolder(executorId, dataSource);
    }

    private static boolean isExtensionEnabled(ExtensionContext extensionContext) {
        try {
            return isOnClasspath("io.micronaut.test.extensions.junit5.MicronautJunit5Extension");
        } catch (Exception e) {
            return false;
        }
    }

    private static Optional<ApplicationContext> getApplicationContext(ExtensionContext extensionContext) {
        ExtensionContext.Store micronautStore = extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class));
        if (micronautStore != null) {
            try {
                io.micronaut.context.ApplicationContext appContext = (io.micronaut.context.ApplicationContext) micronautStore.get(io.micronaut.context.ApplicationContext.class);
                if (appContext != null) {
                    return Optional.of(appContext);
                }
            } catch (ClassCastException ex) {
            }
        }
        return Optional.empty();
    }

    private static DataSource getDataSourceFromMicronautContext(ExtensionContext extensionContext, String beanName) {
        Optional<io.micronaut.context.ApplicationContext> context = getApplicationContext(extensionContext);
        if (context.isPresent()) {
            return beanName.isEmpty() ? context.get().getBean(DataSource.class) : context.get().getBean(DataSource.class, Qualifiers.byName(beanName));
        }
        throw new RuntimeException("Micronaut context is not available for test: " + extensionContext.getTestClass().get().getName());
    }

}
