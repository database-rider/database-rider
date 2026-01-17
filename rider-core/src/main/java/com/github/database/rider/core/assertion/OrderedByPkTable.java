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

    public static OrderedByPkTable create(ITable expected, ITable actual) throws DataSetException {
        return new OrderedByPkTable(expected, getPkMapping(expected, actual));
    }

    private static Map<Integer, Integer> getPkMapping(ITable expected, ITable actual) throws DataSetException {
        final Column[] primaryKeys = actual.getTableMetaData().getPrimaryKeys();
        if (primaryKeys.length == 0) {
            return Collections.emptyMap();
        }
        if (primaryKeys.length == 1) {
            return getSimplePkMapping(expected, actual);
        }
        return getCompositePkMapping(expected, actual);
    }

    private static Map<Integer, Integer> getSimplePkMapping(ITable expected, ITable actual) throws DataSetException {
        final Column primaryKey = actual.getTableMetaData().getPrimaryKeys()[0];
        final Map<Integer, Integer> orderMapping = new HashMap<>();
        final Map<String, Integer> actualPkOrders = getSimplePkOrders(actual, primaryKey);
        final Map<String, Integer> expectedPkOrders = getSimplePkOrders(expected, primaryKey);
        actualPkOrders.forEach((value, rowIndex) -> {
            if (expectedPkOrders.containsKey(value)) {
                orderMapping.put(rowIndex, expectedPkOrders.get(value));
            }
        });
        return orderMapping;
    }

    private static Map<String, Integer> getSimplePkOrders(ITable table, Column primaryKey) throws DataSetException {
        final Map<String, Integer> actualOrders = new HashMap<>();
        final int actualRowCount = table.getRowCount();
        for (int i = 0; i < actualRowCount; i++) {
            final String primaryKeyColumnName = primaryKey.getColumnName();
            final Object actualPkValue = table.getValue(i, primaryKeyColumnName);
            actualOrders.put(String.valueOf(actualPkValue), i);
        }
        return actualOrders;
    }

    private static Map<Integer, Integer> getCompositePkMapping(ITable expected, ITable actual) throws DataSetException {
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