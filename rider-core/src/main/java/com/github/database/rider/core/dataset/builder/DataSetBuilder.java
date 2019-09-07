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
import java.util.LinkedHashMap;
import java.util.Map;
import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;
import java.util.Map.Entry;

public class DataSetBuilder {

    private CachedDataSet dataSet = new CachedDataSet();
    private IDataSetConsumer consumer = new BufferedConsumer(dataSet);
    private final Map<String, TableMetaDataBuilder> tableNameToMetaData = new HashMap<>();
    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    private final DBUnitConfig config;
    private TableBuilder tableBuilder;
    private final Map<String, Object> defaultValues = new LinkedHashMap<>();
    private String currentTableName;
    private final Map<String, Map<String, Object>> tableDefaultValues = new HashMap<>();

    public DataSetBuilder() {
        try {
            consumer.startDataSet();
            config = DBUnitConfig.fromGlobalConfig();
        } catch (DataSetException e) {
            LOGGER.error("Could not create DataSetBuilder.", e);
            throw new RuntimeException("Could not create DataSetBuilder.", e);
        }
    }

    public DataSetBuilder(DBUnitConfig config) {
        try {
            this.config = config;
            consumer.startDataSet();
        } catch (DataSetException e) {
            LOGGER.error("Could not create DataSetBuilder.", e);
            throw new RuntimeException("Could not create DataSetBuilder.", e);
        }

    }

    /**
     * Starts creating a dataset for the given table, ex:
     * <pre>
     * {@code
     *  builder.table("user")
     *         .row()
     *            .column("id", 1)
     *            .column("name", "@dbunit")
     * }
     * </pre>
     * @param tableName name of the table to screate the dataset
     *
     * @return current table builder
     */
    public TableBuilder table(String tableName) {
        tableBuilder = new TableBuilder(this, tableName, config);
        return tableBuilder;
    }

    /**
     * @return a dbunit dataset based on current builder
     */
    public IDataSet build() {
        try {
            if (tableBuilder != null && !tableBuilder.getCurrentRowBuilder().isAdded() && tableBuilder.getCurrentRowBuilder().hasColumns()) {
                add(tableBuilder.getCurrentRowBuilder());
                tableBuilder.getCurrentRowBuilder().setAdded(true);
            }
            endTableIfNecessary();
            consumer.endDataSet();
            return dataSet;
        } catch (DataSetException e) {
            LOGGER.error("Could not create dataset.", e);
            throw new RuntimeException("Could not create DataSet.", e);
        }
    }

    /**
     * Adds a previously created dataset to current builder
     * @param iDataSet dbunit dataset
     * @return current builder
     */
    public DataSetBuilder addDataSet(final IDataSet iDataSet) {
        try {
            IDataSet[] dataSets = {build(), iDataSet};
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
     * Add a previously created row to current dataset, ex:
     * <pre>
     * {@code
     * RowBuilder user1Row = new DataSetBuilder().table("USER")
     *     .row()
     *         .column("id", "1")
     *         .column("name", "user1");
     * RowBuilder user2Row = new DataSetBuilder().table("USER")
     *     .row()
     *         .column("id", "2")
     *         .column("name", "user2");
     *
     * IDataSet iDataSet = builder.add(user1Row).add(user2Row)
     * }
     * </pre>
     *
     * @param row an already built row
     *
     * @return current builder
     */
    public DataSetBuilder add(BasicRowBuilder row) {
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


    /**
     * Adds a default value for the given column for all tables.
     * The default value will be used only if the column is not specified
     *
     * @param columnName name of the column
     * @param value default value associated with the column
     * @return current builder
     */
    public DataSetBuilder defaultValue(String columnName, Object value) {
        defaultValues.put(convertCase(columnName, config), value);
        return this;
    }

    private Object[] extractValues(BasicRowBuilder row, ITableMetaData metaData) throws DataSetException {
        return row.values(metaData.getColumns());
    }

    private void notifyConsumer(Object[] values) throws DataSetException {
        consumer.row(values);
    }

    private ITableMetaData updateTableMetaData(BasicRowBuilder row) throws DataSetException {
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

    private boolean containsKey(String key) {
        return tableNameToMetaData.containsKey(key);
    }


    protected void fillUndefinedColumns(BasicRowBuilder row) {
        if(!defaultValues.isEmpty()) {
            for (Entry<String, Object> entry : defaultValues.entrySet()) {
                if (!row.getColumnsValues().containsKey(entry.getKey())) {
                    row.getColumnsValues().put(entry.getKey(), entry.getValue());
                }
            }
        }

        if(hasDefaulValuesForTable(row.getTableName())) {
            for (Map.Entry<String, Object> column : getDefaultValuesForTable(row.getTableName()).entrySet()) {
                if (!row.getColumnsValues().containsKey(column.getKey())) {
                    row.getColumnsValues().put(column.getKey(), column.getValue());
                }
            }
        }
    }


    protected boolean hasDefaulValuesForTable(String tableName) {
        String key = tableName.toLowerCase();
        return tableDefaultValues.containsKey(key);
    }

    protected Map<String, Object> getDefaultValuesForTable(String tableName) {
        String key = tableName.toLowerCase();
        if(!hasDefaulValuesForTable(key)) {
            return new HashMap<>();
        }
        return tableDefaultValues.get(key);
    }

    protected void addTableDefaultValue(String tableName, String columnName, Object value) {
        String key = tableName.toLowerCase();
        if(!hasDefaulValuesForTable(key)) {
            tableDefaultValues.put(key, new LinkedHashMap<String, Object>());
        }
        tableDefaultValues.get(key).put(convertCase(columnName, config), value);
    }
}
