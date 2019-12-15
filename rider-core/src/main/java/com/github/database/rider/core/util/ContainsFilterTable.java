package com.github.database.rider.core.util;

import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ContainsFilterTable implements ITable {

    /**
     * reference to the original table being wrapped
     */
    private final ITable originalTable;
    /** mapping of filtered rows, i.e, each entry on this list has the value of
     the index on the original table corresponding to the desired index.
     For instance, if the original table is:
     row Value
     0    v1
     1    v2
     2    v3
     3    v4
     And the expected values are:
     row Value
     0   v2
     1   v4
     The new table should be:
     row Value
     0    v2
     1    v4
     Consequently, the mapping will be {1, 3}
     */
    private final List<Integer> filteredRowIndexes;
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(RowFilterTable.class);

    /**
     * Creates a new {@link ITable} where some rows can be filtered out from the original table
     * @param actualTable The table to be wrapped
     * @param expectedTable actualTable will be filtered by this table
     * @throws DataSetException throws DataSetException
     */
    public ContainsFilterTable(ITable actualTable, ITable expectedTable, List<String> ignoredCols) throws DataSetException {
        if ( expectedTable == null || actualTable == null ) {
            throw new IllegalArgumentException( "Constructor cannot receive null arguments" );
        }
        this.originalTable = actualTable;
        // sets the rows for the new table
        // NOTE: this conversion might be an issue for long tables, as it iterates for
        // all values of the original table and that might take time and memory leaks.
        this.filteredRowIndexes = setRows(expectedTable, toUpper(ignoredCols));
    }

    private List<String> toUpper(List<String> ignoredCols) {
        List<String> upperCaseColumns = new ArrayList<>();
        for (String ignoredCol : ignoredCols) {
            upperCaseColumns.add(ignoredCol.toUpperCase());
        }
        return upperCaseColumns;

    }

    private List<Integer> setRows(ITable expectedTable, List<String> ignoredCols) throws DataSetException {

        ITableMetaData tableMetadata = this.originalTable.getTableMetaData();
        this.logger.debug("Setting rows for table {}",  tableMetadata.getTableName() );

        int fullSize = expectedTable.getRowCount();
        List<String> columns = new ArrayList<>();
        if (fullSize > 0) {
            for (Column column : expectedTable.getTableMetaData().getColumns()) {
                columns.add(column.getColumnName());
            }
        }
        List<Integer> filteredRowIndexes = new ArrayList<>();

        for ( int row=0; row<fullSize; row++ ) {
            List<Object> values = new ArrayList<>();
            for (String column : columns) {
                values.add(expectedTable.getValue(row, column));
            }
            Integer actualRowIndex = tableContains(columns, values, filteredRowIndexes, ignoredCols);
            if (actualRowIndex == null) {
                this.logger.debug("Discarding row {}", row);
                continue;
            }

            this.logger.debug("Adding row {}", row);
            filteredRowIndexes.add(actualRowIndex);
        }
        return filteredRowIndexes;
    }

    /**
     * Searches for full match in original table by values from expected table
     * @param columns column names
     * @param values column values
     * @param filteredRowIndexes list of row indexes already found by previous runs
     * @return row index of original table containing all requested values
     * @throws DataSetException throws DataSetException
     */
    private Integer tableContains(List<String> columns, List<Object> values, List<Integer> filteredRowIndexes, List<String> ignoredCols) throws DataSetException {
        int fullSize = this.originalTable.getRowCount();

        for ( int row=0; row<fullSize; row++ ) {
            boolean match = true;
            for (int column = 0; column < columns.size(); column++) {
                if(ignoredCols != null && ignoredCols.contains(columns.get(column).toUpperCase())) {
                    continue;
                }
                if (values.get(column) != null && values.get(column).toString().startsWith("regex:")) {
                    if (!regexMatches(values.get(column).toString(), this.originalTable.getValue(row, columns.get(column)).toString())) {
                        match = false;
                        break;
                    }
                    continue;
                }

                int columnIndex = this.originalTable.getTableMetaData().getColumnIndex(columns.get(column));
                DataType dataType = this.originalTable.getTableMetaData().getColumns()[columnIndex].getDataType();
                if (dataType.compare(values.get(columnIndex), this.originalTable.getValue(row, columns.get(columnIndex))) != 0) {
                    match = false;
                    break;
                }
            }

            if (match && !filteredRowIndexes.contains(row)) {
                return row;
            }
        }
        return null;
    }

    private boolean regexMatches(String expectedValue, String actualValue) {
        Pattern pattern = Pattern.compile(expectedValue.substring(expectedValue.indexOf(':')+1).trim());
        return pattern.matcher(actualValue).matches();
    }

    @Override
    public ITableMetaData getTableMetaData() {
        logger.debug("getTableMetaData() - start");

        return this.originalTable.getTableMetaData();
    }

    @Override
    public int getRowCount() {
        logger.debug("getRowCount() - start");

        return this.filteredRowIndexes.size();
    }

    @Override
    public Object getValue(int row, String column) throws DataSetException
    {
        if(logger.isDebugEnabled())
            logger.debug("getValue(row={}, columnName={}) - start", Integer.toString(row), column);

        int max = this.filteredRowIndexes.size();
        if ( row < max ) {
            int realRow = this.filteredRowIndexes.get(row);
            return this.originalTable.getValue(realRow, column);
        } else {
            throw new RowOutOfBoundsException( "tried to access row " + row +
                    " but rowCount is " + max );
        }
    }
}
