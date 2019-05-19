package com.github.database.rider.core.dataset.builder;

import org.dbunit.dataset.IDataSet;

import java.util.Calendar;
import java.util.Date;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;

public class ColumnBuilder extends BasicRowBuilder {

    private final String[] columns;
    private final TableBuilder tableBuilder;

    public ColumnBuilder(TableBuilder tableBuilder, String tableName, String... columns) {
        super(tableName);
        this.columns = columns;
        this.tableBuilder = tableBuilder;
    }

    /**
     *
     * @param values values to be set on declared columns on {@link TableBuilder#columns(String...)}
     * @return current ColumnBuilder object
     */
    public ColumnBuilder values(Object... values) {
        if (values.length != columns.length) {
            throw new RuntimeException(String.format("Number of columns (%s) for table %s is different than the number of provided values (%s)", columns.length, getTableName(), values.length));
        }
        for (int i = 0; i < columns.length; i++) {
            if(values[i] != null) {//default values
                Object columnValue = values[i];
                if(columnValue instanceof Date || columnValue instanceof Calendar) {
                    columnValue = formatDateValue(columnValue);
                }
                columnNameToValue.put(convertCase(columns[i], config), columnValue);
            }
        }
        saveCurrentRow();
        return this;
    }

    private void saveCurrentRow() {
        tableBuilder.saveCurrentRow(this);
        setAdded(false);
        columnNameToValue.clear();
    }

    public TableBuilder table(String tableName) {
        tableBuilder.saveCurrentRow(this);
        setAdded(false);
        return tableBuilder.getDataSetBuilder().table(tableName);
    }

    /**
     * @return a dbunit dataset based on current builder
     */
    public IDataSet build() {
        tableBuilder.getCurrentRowBuilder().setAdded(true);//in columns-values syntax the row is added on each 'values' call
        return tableBuilder.getDataSetBuilder().build();
    }
}
