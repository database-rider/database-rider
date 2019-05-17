package com.github.database.rider.core.dataset.builder;

import javax.persistence.metamodel.Attribute;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.getColumnNameFromMetaModel;

public class TableBuilder {

    private final DataSetBuilder dataSetBuilder;
    private RowBuilder currentRowBuilder;
    private ColumnBuilder currentColumnBuilder;
    private String tableName;

    public TableBuilder(DataSetBuilder dataSetBuilder, String tableName) {
        this.currentRowBuilder = new RowBuilder(this, tableName);
        this.dataSetBuilder = dataSetBuilder;
        this.tableName = tableName;
    }

    /**
     * Starts row creation for current table:
     * <pre>
     * {@code
     *  builder.table("user")
     *         .row()
     *            .column("id", 1)
     *            .column("name", "@dbunit")
     *         .row()
     *             .column("id", 2)
     *             .column("name", "@dbrider").build();
     * }
     * </pre>
     */
    public RowBuilder row() {
        if(currentRowBuilder.hasColumns()) {
            saveCurrentRow();
            currentRowBuilder.setAdded(false);
            currentRowBuilder.getColumnsValues().clear();
        }
        return currentRowBuilder;
    }

    /**
     * Simplified syntax for row creation which specifies columns only once
     * and then declare values of each row:
     * <pre>
     * {@code
     *  builder.table("user")
     *         .columns("id", "name")
     *         .values(1,"@dbunit")
     *         .values(2,"@dbrider").build();
     * }
     * </pre>
     *
     * @param columns
     * @return a column builder responsible for creating rows using simplified syntax
     */
    public ColumnBuilder columns(String... columns) {
        if(currentColumnBuilder != null && currentColumnBuilder.hasColumns()) {
            saveCurrentRow(currentColumnBuilder);
            currentColumnBuilder.getColumnsValues().clear();
        }
        currentColumnBuilder = new ColumnBuilder(this, tableName, columns);

        return currentColumnBuilder;
    }


    /**
     * Simplified syntax for row creation, using JPA metalmodel, specifying columns only once
     * and then declare values of each row:
     * <pre>
     * {@code
     *  builder.table("user")
     *         .columns(User_id, User_name)
     *         .values(1,"@dbunit")
     *         .values(2,"@dbrider").build();
     * }
     * </pre>
     *
     * @param columns
     * @return a column builder responsible for creating rows using simplified syntax
     */
    public ColumnBuilder columns(Attribute... columns) {
        String[] columnList = new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            columnList[i] = getColumnNameFromMetaModel(columns[i]);
        }
        return columns(columnList);
    }

    protected RowBuilder getCurrentRowBuilder() {
        return currentRowBuilder;
    }

    protected  DataSetBuilder getDataSetBuilder() {
        return dataSetBuilder;
    }

    protected void saveCurrentRow() {
        currentRowBuilder.setAdded(true);
        dataSetBuilder.add(currentRowBuilder);
    }

    protected void saveCurrentRow(BasicRowBuilder rowBuilder) {
        rowBuilder.setAdded(true);
        dataSetBuilder.add(rowBuilder);
    }

    /**
     * Adds a default value for the given column in current table.
     * The default value will be used only if the column is not specified
     *
     * @param columnName
     * @param value
     */
    public TableBuilder defaultValue(String columnName, Object value) {
        dataSetBuilder.addTableDefaultValue(tableName, columnName, value);
        return this;
    }

}
