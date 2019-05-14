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
import org.dbunit.dataset.*;
import org.dbunit.dataset.stream.BufferedConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;

public class DataSetBuilder {

    private CachedDataSet dataSet = new CachedDataSet();
    private IDataSetConsumer consumer = new BufferedConsumer(dataSet);
    private final Map<String, TableMetaDataBuilder> tableNameToMetaData = new HashMap<>();
    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    private final DBUnitConfig config;
    private DataRowBuilder currentRowBuilder;
    private final Map<String, Object> defaultValues = new HashMap<>();
    private String currentTableName;


    public DataSetBuilder() {
        try {
            consumer.startDataSet();
            config = DBUnitConfig.fromGlobalConfig();
        } catch (DataSetException e) {
            LOGGER.error("Could not create DataSetBuilder.", e);
            throw new RuntimeException("Could not create DataSetBuilder.", e);
        }
    }

    /**
     * Starts a new row for the given tableName
     * @param tableName
     */
    public DataRowBuilder table(String tableName) {
        currentRowBuilder = new DataRowBuilder(this, tableName);
        return currentRowBuilder;
    }

    public IDataSet build() {
        try {
            if (currentRowBuilder != null && !currentRowBuilder.isAdded()) {
               add(currentRowBuilder);
               currentRowBuilder.setAdded(true);
            }
            endTableIfNecessary();
            consumer.endDataSet();
            return dataSet;
        } catch (DataSetException e) {
            LOGGER.error("Could not create dataset.", e);
            throw new RuntimeException("Could not create DataSet.", e);
        }
    }

    public DataSetBuilder addDataSet(final IDataSet newDataSet) {
        try {
            IDataSet[] dataSets = {build(), newDataSet};
            CompositeDataSet composite = new CompositeDataSet(dataSets);
            this.dataSet = new CachedDataSet(composite);
            consumer = new BufferedConsumer(this.dataSet);
            return this;
        } catch (DataSetException e) {
            LOGGER.error("Could not add dataset.", e);
            throw new RuntimeException("Could not add dataset.", e);
        }
    }

    /**
     * Add a row to current dataset
     * @return
     */
    public DataSetBuilder add(BasicDataRowBuilder row) {
        try {
            fillUndefinedColumns(row);
            ITableMetaData metaData = updateTableMetaData(row);
            Object[] values = extractValues(row, metaData);
            notifyConsumer(values);
            return this;
        } catch (DataSetException e) {
            LOGGER.error("Could not add dataset row.", e);
            throw new RuntimeException("Could not add dataset row.", e);
        }
    }

    public DataSetBuilder defaultValue(String columnName, Object value) {
        defaultValues.put(convertCase(columnName, config), value);
        return this;
    }

    private Object[] extractValues(BasicDataRowBuilder row, ITableMetaData metaData) throws DataSetException {
        return row.values(metaData.getColumns());
    }

    private void notifyConsumer(Object[] values) throws DataSetException {
        consumer.row(values);
    }

    private ITableMetaData updateTableMetaData(BasicDataRowBuilder row) throws DataSetException {
        TableMetaDataBuilder builder = metaDataBuilderFor(row.getTableName());
        int previousNumberOfColumns = builder.numberOfColumns();

        ITableMetaData metaData = builder.with(row.toMetaData()).build();
        int newNumberOfColumns = metaData.getColumns().length;

        boolean addedNewColumn = newNumberOfColumns > previousNumberOfColumns;
        handleTable(metaData, addedNewColumn);

        return metaData;
    }

    private void handleTable(ITableMetaData metaData, boolean addedNewColumn) throws DataSetException {
        if (isNewTable(metaData.getTableName())) {
            endTableIfNecessary();
            startTable(metaData);
        } else if (addedNewColumn) {
            startTable(metaData);
        }
    }

    private void startTable(ITableMetaData metaData) throws DataSetException {
        currentTableName = metaData.getTableName();
        consumer.startTable(metaData);
    }

    private void endTable() throws DataSetException {
        consumer.endTable();
        currentTableName = null;
    }

    private void endTableIfNecessary() throws DataSetException {
        if (hasCurrentTable()) {
            endTable();
        }
    }

    private boolean hasCurrentTable() {
        return currentTableName != null;
    }

    private boolean isNewTable(String tableName) {
        return currentTableName == null || !convertCase(currentTableName, config).equals(convertCase(tableName, config));
    }

    private TableMetaDataBuilder metaDataBuilderFor(String tableName) {
        String key = convertCase(tableName, config);
        if (containsKey(key)) {
            return tableNameToMetaData.get(key);
        }
        TableMetaDataBuilder builder = createNewTableMetaDataBuilder(tableName);
        tableNameToMetaData.put(key, builder);
        return builder;
    }

    protected TableMetaDataBuilder createNewTableMetaDataBuilder(String tableName) {
        return new TableMetaDataBuilder(tableName);
    }

    public boolean containsTable(String tableName) {
        return containsKey(convertCase(tableName, config));
    }

    private boolean containsKey(String key) {
        return tableNameToMetaData.containsKey(key);
    }

    public void setCurrentRowBuilder(DataRowBuilder dataRowBuilder) {
        this.currentRowBuilder = dataRowBuilder;
    }

    protected DataRowBuilder getCurrentRowBuilder() {
        return currentRowBuilder;
    }

    public void fillUndefinedColumns(BasicDataRowBuilder row) {
        if(!defaultValues.isEmpty()) {
            for (String column : defaultValues.keySet()) {
                if (!row.columnNameToValue.containsKey(column)) {
                    row.columnNameToValue.put(column, defaultValues.get(column));
                }
            }
        }
    }
}
