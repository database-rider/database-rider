package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.configuration.DBUnitConfig;
import org.dbunit.dataset.Column;

public class BuilderUtil {

    public static String convertColumnCase(Column column, DBUnitConfig config) {
        return convertCase(column.getColumnName(), config);
    }

    public static String convertCase(String value, DBUnitConfig config) {
        if (value != null && !config.isCaseSensitiveTableNames()) {
            if (Orthography.UPPERCASE == config.getCaseInsensitiveStrategy()) {
                value = value.toUpperCase();
            } else {
                value = value.toLowerCase();
            }
        }
        return value;
    }

}
