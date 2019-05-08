package com.github.database.rider.core.dataset.builder;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.ColumnSpec;
import org.dbunit.dataset.builder.DataRowBuilder;
import org.dbunit.dataset.builder.DataSetBuilder;
import org.dbunit.dataset.datatype.DataType;

import javax.persistence.metamodel.SingularAttribute;
import java.math.BigDecimal;
import java.util.Date;

public class RiderDataRowBuilder extends DataRowBuilder {

    protected RiderDataRowBuilder(DataSetBuilder dataSet, String tableName) {
        super(dataSet, tableName);
    }

    protected Column createColumn(String columnName) {
        Object value = columnNameToValue.get(columnName);
        DataType columnType = DataType.UNKNOWN;
        if(value instanceof Integer) {
            columnType = DataType.INTEGER;
        }
        if(value instanceof Long) {
            columnType = DataType.BIGINT_AUX_LONG;
        }
        if(value instanceof Double) {
            columnType = DataType.DOUBLE;
        }
        if(value instanceof Float) {
            columnType = DataType.FLOAT;
        }
        if(value instanceof Date) {
            columnType = DataType.DATE;
        }
        if(value instanceof Boolean) {
            columnType = DataType.BOOLEAN;
        }
        if(value instanceof BigDecimal) {
            columnType = DataType.DECIMAL;
        }
        if(value instanceof Number) {
            columnType = DataType.NUMERIC;
        }
        return new Column(columnName, columnType);
    }

    @Override
    public <T> RiderDataRowBuilder with(ColumnSpec<T> column, T value) {
        return with(column.name(), value);
    }

    @Override
    public RiderDataRowBuilder with(String columnName, Object value) {
        put(columnName, value);
        return this;
    }

    public <T> RiderDataRowBuilder with(SingularAttribute column, T value) {
        return with(column.getName(), value);
    }

    @Override
    public RiderDataSetBuilder add() throws DataSetException {
        return (RiderDataSetBuilder) super.add();
    }
}
