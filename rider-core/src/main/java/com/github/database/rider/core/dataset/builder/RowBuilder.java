package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import org.dbunit.dataset.IDataSet;

import jakarta.persistence.metamodel.Attribute;
import java.util.Calendar;
import java.util.Date;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.getColumnNameFromMetaModel;

public class RowBuilder extends BasicRowBuilder {

    private final TableBuilder tableBuilder;

    protected RowBuilder(TableBuilder tableBuilder, String tableName, DBUnitConfig config) {
        super(tableName, config);
        this.tableBuilder = tableBuilder;
    }

    /**
     * Adds a column to current row
     *
     * @param column column definition
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(ColumnSpec column, Object value) {
        super.column(column.name(), value);
        return this;
    }

    /**
     * Adds a date column to current row
     * The date will be converted to dbunit default format (yyyy-MM-dd HH:mm:ss)
     * Note that if the date has hour/minutes/seconds unset (0) then 'yyyy-MM-dd' will be used as format
     * @param columnName the column name
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(String columnName, Date value) {
        put(columnName, formatDateValue(value));
        return this;
    }

    /**
     * Adds a calendar column to current row
     * The calendar will be converted to dbunit default format (yyyy-MM-dd HH:mm:ss)
     * Note that if the date has hour/minutes/seconds unset (0) then 'yyyy-MM-dd' will be used as format
     * @param columnName the column name
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(String columnName, Calendar value) {
        put(columnName, formatDateValue(value));
        return this;
    }

    /**
     * Type safe approach to add a column to current row based on JPA metamodel.
     *
     * The actual name of the column will be extracted from the metamodel.
     *
     * @param column JPA metamodel column
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(Attribute column, Object value) {
        String columnName = getColumnNameFromMetaModel(column);
        super.column(columnName, value);
        return this;
    }

    /**
     * Adds a column to current row
     * @param columnName the column name
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(String columnName, Object value) {
        super.column(columnName, value);
        return this;
    }

    /**
     * Adds a calendar column to current row based on JPA metamodel
     * The calendar will be converted to dbunit default format (yyyy-MM-dd HH:mm:ss)
     * Note that if the date has hour/minutes/seconds unset (0) then 'yyyy-MM-dd' will be used as format
     * @param column JPA metamodel column
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(Attribute column, Calendar value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    /**
     * Adds a date column to current row based on JPA metamodel
     * The date will be converted to dbunit default format (yyyy-MM-dd HH:mm:ss)
     * Note that if the date has hour/minutes/seconds unset (0) then 'yyyy-MM-dd' will be used as format
     * @param column JPA metamodel column
     * @param value the column value
     * @return current row builder
     */
    public RowBuilder column(Attribute column, Date value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    /**
     * starts a new row for current table
     * @return current row builder
     */
    public RowBuilder row() {
        return tableBuilder.row();
    }


    /**
     * Starts creating rows for a new table
     * @param tableName table which new rows will be added
     * @return a table builder
     */
    public TableBuilder table(String tableName) {
        tableBuilder.saveCurrentRow(); //save current row  every time a new table is started
        return tableBuilder.getDataSetBuilder().table(tableName);
    }

    /**
     * @return a dbunit dataset based on current builder
     */
    public IDataSet build() {
        return tableBuilder.getDataSetBuilder().build();
    }

}
