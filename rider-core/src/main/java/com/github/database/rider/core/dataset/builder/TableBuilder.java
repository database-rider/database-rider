package com.github.database.rider.core.dataset.builder;

import org.dbunit.dataset.IDataSet;

import java.util.HashMap;
import java.util.Map;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;

public class TableBuilder {

    private final DataSetBuilder dataSetBuilder;
    private RowBuilder currentRowBuilder;
    private String tableName;

    public TableBuilder(DataSetBuilder dataSetBuilder, String tableName) {
        this.currentRowBuilder = new RowBuilder(this, tableName);
        this.dataSetBuilder = dataSetBuilder;
        this.tableName = tableName;
    }

    public RowBuilder row() {
        if(currentRowBuilder.hasColumns()) {
            saveCurrentRow();
            currentRowBuilder.setAdded(false);
            currentRowBuilder.columnNameToValue.clear();
        }
        return currentRowBuilder;
    }

    protected RowBuilder getCurrentRowBuilder() {
        return currentRowBuilder;
    }

    protected DataSetBuilder getDataSetBuilder() {
        return dataSetBuilder;
    }

    protected void saveCurrentRow() {
        currentRowBuilder.setAdded(true);
        dataSetBuilder.add(currentRowBuilder);
    }

    public TableBuilder defaultValue(String columnName, Object value) {
        dataSetBuilder.addTableDefaultValue(tableName, columnName, value);
        return this;
    }

}
