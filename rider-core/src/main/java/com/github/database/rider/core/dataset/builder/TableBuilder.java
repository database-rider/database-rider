package com.github.database.rider.core.dataset.builder;

public class TableBuilder {

    private final DataSetBuilder dataSetBuilder;
    private RowBuilder currentRowBuilder;
    private String tableName;

    public TableBuilder(DataSetBuilder dataSetBuilder, String tableName) {
        this.currentRowBuilder = new RowBuilder(this, tableName);
        this.dataSetBuilder = dataSetBuilder;
        this.tableName = tableName;
    }

    /**
     * starts a row creation for given table
     * @return
     */
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

    /**
     * Adds a default value for the given column in current table
     * The default value will be used only if the column was not specified
     *
     * @param columnName
     * @param value
     */
    public TableBuilder defaultValue(String columnName, Object value) {
        dataSetBuilder.addTableDefaultValue(tableName, columnName, value);
        return this;
    }

}
