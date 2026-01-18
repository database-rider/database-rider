package com.github.database.rider.core.replacers;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Replacer which replaces "[INCLUDE]path/to/the/file" with actual content of the path/to/the/file
 * File should be available in the classpath.
 */
public class IncludeReplacer implements Replacer {
    private static final String INCLUDE_PREFIX = "[INCLUDE]";

    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        try {
            String[] tableNames = dataSet.getTableNames();
            for (String tableName : tableNames) {
                addReplacementsForTable(tableName, dataSet);
            }
        } catch (DataSetException e) {
            throw new RuntimeException("Unable to perform classpath file replacements", e);
        }
    }

    private void addReplacementsForTable(String tableName, ReplacementDataSet dataSet) throws DataSetException {
        ITable table = dataSet.getTable(tableName);
        Column[] columns = table.getTableMetaData().getColumns();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (Column column : columns) {
                final Object columnValue = table.getValue(i, column.getColumnName());
                if (columnValue instanceof String) {
                    final String stringValue = (String) columnValue;
                    if (hasIncludePrefix(stringValue)) {
                        final String filePath = removeIncludePrefix(stringValue);
                        final String fileContent = readFile(filePath);
                        dataSet.addReplacementSubstring(stringValue, fileContent);
                    }
                }
            }
        }
    }

    private boolean hasIncludePrefix(String value) {
        return value != null && value.startsWith(INCLUDE_PREFIX);
    }

    private String removeIncludePrefix(String columnValue) {
        return columnValue.substring(INCLUDE_PREFIX.length());
    }

    private String readFile(String filePath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            String fileNotFoundError = String.format("Unable to read file [%s] from the classpath", filePath);
            throw new RuntimeException(fileNotFoundError);
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}
