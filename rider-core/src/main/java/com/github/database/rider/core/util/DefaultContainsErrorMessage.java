package com.github.database.rider.core.util;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.ArrayList;
import java.util.List;

public class DefaultContainsErrorMessage implements ContainsErrorMessage {

    private static final String COLUMN_DELIMITER = "  | ";
    private final List<String> columnNames;
    private List<Object> values;
    private List<Integer> columnWidths;
    private final ITable originalTable;
    private StringBuilder buffer;
    private boolean match = false;

    public DefaultContainsErrorMessage(List<String> columnNames, ITable originalTable) {
        this.columnNames = columnNames;
        this.originalTable = originalTable;
    }

    @Override
    public void initWithValues(List<Object> values) throws DataSetException {
        buffer = new StringBuilder();
        this.match = false;
        this.values = values;
        final int rowCount = originalTable.getRowCount();
        final List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            int width = Math.max(columnNames.get(i).length(), String.valueOf(values.get(i)).length());
            for (int r = 0; r < rowCount; r++) {
                final Object value = originalTable.getValue(r, columnNames.get(i));
                width = Math.max(width, String.valueOf(value).length());
            }
            widths.add(width);
        }
        this.columnWidths = widths;
    }

    @Override
    public void addTableHeader() {
        final int rowWidth = columnWidths.stream().mapToInt(integer -> integer + COLUMN_DELIMITER.length()).sum();
        final String delimiterLine = repeat("-", rowWidth) + "\n";
        buffer.append(delimiterLine);
        buffer.append(String.format(" Table '%s'%n", originalTable.getTableMetaData().getTableName()));
        buffer.append(delimiterLine);
        for (int i = 0; i < columnWidths.size(); i++) {
            final Integer columnWidth = columnWidths.get(i);
            final String columnName = columnNames.get(i);
            addValue(columnName, columnWidth);
        }
        nextLine();
        buffer.append(delimiterLine);
        for (int i = 0; i < columnWidths.size(); i++) {
            final Integer columnWidth = columnWidths.get(i);
            final String value = String.valueOf(values.get(i));
            addValue(value, columnWidth);
        }
        buffer.append(" Expected values\n\n");
    }

    private String repeat(String string, int times) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(string);
        }
        return sb.toString();
    }

    private void addValue(String value, Integer columnWidth) {
        buffer.append(value).append(repeat(" ", columnWidth - value.length())).append(COLUMN_DELIMITER);
    }

    @Override
    public void addRow(int row) throws DataSetException {
        for (int i = 0; i < columnWidths.size(); i++) {
            final Integer length = columnWidths.get(i);
            final String value = String.valueOf(originalTable.getValue(row, columnNames.get(i)));
            addValue(value, length);
        }
    }

    @Override
    public void addFail(int column, int row) throws DataSetException {
        final String columnName = columnNames.get(column);
        buffer.append(String.format(" Failed in '%s' Expected: '%s' Actual: '%s'",
                   columnName,
                   values.get(column),
                   originalTable.getValue(row, columnName)));
    }

    @Override
    public void setMatch() {
        match = true;
    }

    @Override
    public void nextLine() {
        buffer.append("\n");
    }

    @Override
    public void print() {
        if (!match) {
            logger.error("Mismatch found:\n" + buffer.toString());
        }
    }

}
