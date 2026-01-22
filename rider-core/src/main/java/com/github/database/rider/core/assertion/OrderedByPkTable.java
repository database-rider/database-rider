package com.github.database.rider.core.assertion;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;

public class OrderedByPkTable implements ITable {

    private final ITable expected;
    private final Map<Integer, Integer> primaryKeyOrderMapping;

    public OrderedByPkTable(ITable expected, Map<Integer, Integer> primaryKeyOrderMapping) {
        this.expected = expected;
        this.primaryKeyOrderMapping = primaryKeyOrderMapping;
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return expected.getTableMetaData();
    }

    @Override
    public int getRowCount() {
        return expected.getRowCount();
    }

    @Override
    public Object getValue(int row, String column) throws DataSetException {
        final int mappedRow = primaryKeyOrderMapping.getOrDefault(row, row);
        return expected.getValue(mappedRow, column);
    }

    public static ITable wrapIfNeeded(ITable expected, ITable actual, final String[] ignoreCols) throws DataSetException {
        return shouldWrap(actual, ignoreCols) ? new OrderedByPkTable(expected, getPkMapping(expected, actual)) : expected;
    }

    private static boolean shouldWrap(final ITable actual, final String[] ignoreCols) throws DataSetException {
        final Column[] primaryKeys = actual.getTableMetaData().getPrimaryKeys();
        if (primaryKeys.length == 0) {
            return false;
        }
        for (int i = 0; i < primaryKeys.length; i++) {
            final String columnName = primaryKeys[i].getColumnName();
            for (int j = 0; j < ignoreCols.length; j++) {
                if (ignoreCols[j].equalsIgnoreCase(columnName)) {
                    // we cannot guarantee correct mapping for incomplete complex primary key, so just disable it.
                    return false;
                }
            }
        }
        return true;
    }

    private static Map<Integer, Integer> getPkMapping(ITable expected, ITable actual) throws DataSetException {
        final Column[] primaryKeys = actual.getTableMetaData().getPrimaryKeys();
        final Map<Integer, Integer> orderMapping = new HashMap<>();
        final Map<List<String>, Integer> actualPkOrders = getCompositePkOrders(actual, primaryKeys);
        final Map<List<String>, Integer> expectedPkOrders = getCompositePkOrders(expected, primaryKeys);
        actualPkOrders.forEach((value, rowIndex) -> {
            if (expectedPkOrders.containsKey(value)) {
                orderMapping.put(rowIndex, expectedPkOrders.get(value));
            }
        });
        return orderMapping;
    }

    private static Map<List<String>, Integer> getCompositePkOrders(ITable table, Column[] primaryKeys) throws DataSetException {
        final Map<List<String>, Integer> actualOrders = new HashMap<>();
        final int actualRowCount = table.getRowCount();
        for (int i = 0; i < actualRowCount; i++) {
            final List<String> pkValues = new ArrayList<>();
            for (int j = 0; j < primaryKeys.length; j++) {
                final String primaryKeyColumnName = primaryKeys[j].getColumnName();
                final Object actualPkValue = table.getValue(i, primaryKeyColumnName);
                pkValues.add(String.valueOf(actualPkValue));
            }
            actualOrders.put(Collections.unmodifiableList(pkValues), i);
        }
        return actualOrders;
    }
}