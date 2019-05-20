/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002-2008, DbUnit.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.util.DateUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;
import static com.github.database.rider.core.dataset.builder.BuilderUtil.resolveColumnDataType;

public class BasicRowBuilder {

    private final String tableName;
    protected final DBUnitConfig config;
    protected final Map<String, Object> columnNameToValue = new LinkedHashMap<>();
    private boolean added;

    public BasicRowBuilder(String tableName) {
        this.config = DBUnitConfig.fromGlobalConfig();
        this.tableName = convertCase(tableName,config);
    }

    /**
     * Added the column to the Data.
     *
     * @param columnName the name of the column.
     * @param value      the value the column should have.
     * @return the current object.
     */
    public BasicRowBuilder column(String columnName, Object value) {
        columnNameToValue.put(convertCase(columnName, config), value);
        return this;
    }

    protected Object[] values(Column[] columns) {
        Object[] values = new Object[columns.length];
        int index = 0;
        for (Column column : columns) {
            if(column != null && !"".equals(column.getColumnName())) {
                values[index++] = getValue(column);
            }
        }
        return values;
    }

    protected ITableMetaData toMetaData() {
        Column[] columns = new Column[numberOfColumns()];
        int index = 0;
        for (String columnName : columnNameToValue.keySet()) {
            columns[index++] = createColumn(columnName);
        }
        return createMetaData(columns);
    }

    protected int numberOfColumns() {
        return columnNameToValue.size();
    }

    protected Map<String, Object> getColumnsValues() {
        return columnNameToValue;
    }

    protected ITableMetaData createMetaData(Column[] columns) {
        return new DefaultTableMetaData(tableName, columns);
    }

    protected Column createColumn(String columnName) {
        Object value = columnNameToValue.get(columnName);
        DataType columnType = resolveColumnDataType(value);
        return new Column(columnName, columnType);
    }

    protected String getTableName() {
        return tableName;
    }

    protected void put(String columnName, Object value) {
        columnNameToValue.put(convertCase(columnName, config), value);
    }

    protected Object getValue(Column column) {
        return getValue(column.getColumnName());
    }

    protected Object getValue(String columnName) {
        return columnNameToValue.get(columnName);
    }

    protected boolean hasColumns() {
        return !columnNameToValue.isEmpty();
    }

    /**
     * @return a boolean indicating the current row was already added to the dataset being build
     */
    protected boolean isAdded() {
        return added;
    }

    protected void setAdded(boolean added) {
        this.added = added;
    }

    protected Object formatDateValue(Object value) {
        if(value instanceof Date) {
            return DateUtils.format((Date) value);
        } else if(value instanceof Calendar) {
            return DateUtils.format((Calendar) value);
        }
        return value;
    }

}
