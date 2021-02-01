package com.github.database.rider.core.filter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.List;

public class RiderPrimaryKeyFilter implements IColumnFilter {

    private List<String> disablePrimaryKeyForTables;

    public RiderPrimaryKeyFilter(List<String> disablePrimaryKeyForTables) {
        this.disablePrimaryKeyForTables = disablePrimaryKeyForTables;
    }

    @Override
    public boolean accept(String tableName, Column column) {
        if (tableName == null || disablePrimaryKeyForTables == null || disablePrimaryKeyForTables.isEmpty()) {
            return false;
        }
        for (String disablePrimaryKeyForTable : disablePrimaryKeyForTables) {
            if (tableName.equalsIgnoreCase(disablePrimaryKeyForTable)) {
                return true;
            }
        }
        return false;
    }
}
