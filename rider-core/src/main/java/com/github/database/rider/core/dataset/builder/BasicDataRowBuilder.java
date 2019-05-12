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
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;

public class BasicDataRowBuilder {


    private final String tableName;
    protected final DBUnitConfig config;
    protected final Map<String, Object> columnNameToValue = new LinkedHashMap<>();
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public BasicDataRowBuilder(String tableName) {
        this.config = DBUnitConfig.fromGlobalConfig();
        this.tableName = BuilderUtil.convertCase(tableName,config);
    }


    /**
     * Added the column to the Data.
     *
     * @param columnName the name of the column.
     * @param value      the value the column should have.
     * @return the current object.
     */
    public BasicDataRowBuilder column(String columnName, Object value) {
        columnNameToValue.put(BuilderUtil.convertCase(columnName, config), value);
        return this;
    }

    public Object[] values(Column[] columns) {
        Object[] values = new Object[columns.length];
        int index = 0;
        for (Column column : columns) {
            values[index++] = getValue(column);
        }
        return values;
    }

    public ITableMetaData toMetaData() {
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


    protected ITableMetaData createMetaData(Column[] columns) {
        return new DefaultTableMetaData(tableName, columns);
    }

    protected Column createColumn(String columnName) {
        Object value = columnNameToValue.get(columnName);
        DataType columnType = DataType.UNKNOWN;
        if (value instanceof Integer) {
            columnType = DataType.INTEGER;
        }
        if (value instanceof Long) {
            columnType = DataType.BIGINT_AUX_LONG;
        }
        if (value instanceof Double) {
            columnType = DataType.DOUBLE;
        }
        if (value instanceof Float) {
            columnType = DataType.FLOAT;
        }
        if (value instanceof Date) {
            columnType = DataType.DATE;
        }
        if (value instanceof Boolean) {
            columnType = DataType.BOOLEAN;
        }
        if (value instanceof BigDecimal) {
            columnType = DataType.DECIMAL;
        }
        if (value instanceof Number) {
            columnType = DataType.NUMERIC;
        }
        return new Column(columnName, columnType);
    }

    protected String getTableName() {
        return tableName;
    }

    protected void put(String columnName, Object value) {
        columnNameToValue.put(BuilderUtil.convertCase(columnName, config), value);
    }

    protected Object getValue(Column column) {
        return getValue(column.getColumnName());
    }

    protected Object getValue(String columnName) {
        return columnNameToValue.get(columnName);
    }


    protected boolean existsValue(String columnName) {
        return columnNameToValue.containsKey(columnName);
    }


    protected boolean isHibernateOnClasspath() {
        return isOnClasspath("org.hibernate.Session");
    }

    protected boolean isEclipseLinkOnClasspath() {
        return isOnClasspath("org.eclipse.persistence.mappings.DirectToFieldMapping");
    }
}
