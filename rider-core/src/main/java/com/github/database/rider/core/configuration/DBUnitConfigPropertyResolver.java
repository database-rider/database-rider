package com.github.database.rider.core.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves dbunit properties from system properties and environment variables.
 * In order to resolve the property, the value must be in the format of ${property_value}
 * System properties take precedence over environment variables
 */
final class DBUnitConfigPropertyResolver {

    private DBUnitConfigPropertyResolver() {
    }

    static <T> T resolveProperty(final T originalValue) {
        if (originalValue != null && originalValue.toString().startsWith("${")) {
            String propertyName = originalValue.toString().replaceAll("\\$\\{(.*)}", "$1");
            String value = System.getProperty(propertyName) != null ? System.getProperty(propertyName) : System.getenv(propertyName);
            if (value != null) {
                return (T) value;
            }
        }
        return originalValue;
    }

    static Map<String, Object> resolveProperties(final Map<String, Object> originalProperties) {
        final Map<String, Object> resolvedProperties = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalProperties.entrySet()) {
            resolvedProperties.put(entry.getKey(), resolveProperty(entry.getValue()));
        }
        return resolvedProperties;
    }
}
