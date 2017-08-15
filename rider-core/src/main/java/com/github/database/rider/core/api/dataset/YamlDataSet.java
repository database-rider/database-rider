package com.github.database.rider.core.api.dataset;

/**
 * Created by rafael-pestano on 22/07/2015.
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.datatype.DataType;
import org.yaml.snakeyaml.Yaml;

import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.configuration.DBUnitConfig;

public class YamlDataSet implements IDataSet {

    private Map<String, MyTable> tables = new HashMap<String, MyTable>();

    private DBUnitConfig dbUnitConfig;

    public YamlDataSet(InputStream source) {
        this(source, null);

    }

    public YamlDataSet(InputStream source, DBUnitConfig dbUnitConfig) {
        if (dbUnitConfig != null) {
            this.dbUnitConfig = dbUnitConfig;
        }
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> data = (Map<String, List<Map<String, Object>>>) new Yaml().load(source);
        if (data != null) {
            for (Map.Entry<String, List<Map<String, Object>>> ent : data.entrySet()) {
                String tableName = ent.getKey();
                List<Map<String, Object>> rows = ent.getValue();
                createTable(tableName, rows);
            }
        }
    }

    class MyTable implements ITable {
        String name;

        List<Map<String, Object>> data;

        ITableMetaData meta;

        MyTable(String name, List<String> columnNames) {
            this.name = name;
            this.data = new ArrayList<Map<String, Object>>();
            meta = createMeta(name, columnNames);
        }

        ITableMetaData createMeta(String name, List<String> columnNames) {
            Column[] columns = null;
            if (columnNames != null) {
                columns = new Column[columnNames.size()];
                for (int i = 0; i < columnNames.size(); i++)
                    columns[i] = new Column(columnNames.get(i), DataType.UNKNOWN);
            } else {
                columns = new Column[0];
            }
            return new DefaultTableMetaData(name, columns);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public ITableMetaData getTableMetaData() {
            return meta;
        }

        @Override
        public Object getValue(int row, String column) throws DataSetException {
            if (data.size() <= row)
                throw new RowOutOfBoundsException("" + row);
            return data.get(row).get(applyCaseInsensitivity(column)); // issue #37
        }

        public void addRow(Map<String, Object> values) {
            data.add(convertMap(values));
        }

        Map<String, Object> convertMap(Map<String, Object> values) {
            Map<String, Object> row = new HashMap<String, Object>();
            for (Map.Entry<String, Object> ent : values.entrySet()) {
                row.put(applyCaseInsensitivity(ent.getKey()), ent.getValue()); // issue #37
            }
            return row;
        }

    }

    MyTable createTable(String name, List<Map<String, Object>> rows) {
        MyTable table = new MyTable(applyCaseInsensitivity(name), applyCase(getColumns(rows))); // issue #37
        if (rows != null) {
            for (Map<String, Object> values : rows)
                table.addRow(values);
        }
        tables.put(name.toUpperCase(), table);
        return table;
    }

    public List<String> getColumns(List<Map<String, Object>> rows) {
        if (rows != null) {
            Set<String> columns = new HashSet<String>();
            for (Map<String, Object> row : rows) {
                columns.addAll(applyCase(new ArrayList<>(row.keySet()))); // issue #37
            }

            return new ArrayList<String>(columns);
        }
        return null;
    }

    @Override
    public ITable getTable(String tableName) throws DataSetException {
        return tables.get(tableName.toUpperCase());
    }

    @Override
    public ITableMetaData getTableMetaData(final String tableName) throws DataSetException {
        MyTable myTable = tables.get(tableName.toUpperCase());
        if (myTable != null) {
            return myTable.getTableMetaData();
        }
        return null;
    }

    @Override
    public String[] getTableNames() throws DataSetException {
        ITable[] tables = getTables();
        String[] tableNames = new String[tables.length];

        for (int i = 0; i < tables.length; i++) {
            tableNames[i] = tables[i].getTableMetaData().getTableName();
        }

        return tableNames;
    }

    @Override
    public ITable[] getTables() throws DataSetException {
        return tables.values().toArray(new ITable[tables.size()]);
    }

    @Override
    public ITableIterator iterator() throws DataSetException {
        return new DefaultTableIterator(getTables());
    }

    @Override
    public ITableIterator reverseIterator() throws DataSetException {
        return new DefaultTableIterator(getTables(), true);
    }

    @Override
    public boolean isCaseSensitiveTableNames() {
        // is a Boolean object for sure, add null-safety
        Boolean result = (Boolean) dbUnitConfig.getProperties().get("caseSensitiveTableNames");
        return Boolean.TRUE.equals(result);
    }

    public boolean isCaseInsensitiveStrategyLowerCase() {
        Object strategy = dbUnitConfig.getProperties().get("caseInsensitiveStrategy");
        Orthography result = (strategy == null) ? Orthography.UPPERCASE : Orthography.valueOf(String.valueOf(strategy));
        return Orthography.LOWERCASE.equals(result);
    }

    /**
     * Applies the case-insensitive strategy (orthography) to the given <code>name</code>, if
     * {@link #isCaseSensitiveTableNames()} is <code>false</code>.
     * 
     * @param name The identifier name
     * @return The adjusted identifier name
     * @see Orthography
     */
    String applyCaseInsensitivity(String name) {
        return (name != null)
                ? isCaseSensitiveTableNames() ? name : isCaseInsensitiveStrategyLowerCase()
                        ? name.toLowerCase(Locale.ENGLISH)
                        : name.toUpperCase(Locale.ENGLISH)
                : null;
    }

    private List<String> applyCase(List<String> names) {
        if (names != null) {
            for (int i = 0; i < names.size(); i++) {
                names.set(i, applyCaseInsensitivity(names.get(i)));
            }
        }
        return names;
    }
}
