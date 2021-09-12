package com.github.database.rider.core.assertion;

import com.github.database.rider.core.script.ScriptEngineManagerWrapper;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.assertion.comparer.value.ValueComparer;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by rmpestano on 5/28/16.
 */
public class DataSetAssert extends DbUnitAssert {

    private static final Logger logger = LoggerFactory.getLogger(DbUnitAssert.class);

    private final ScriptEngineManagerWrapper manager = ScriptEngineManagerWrapper.getInstance();

    private List<Integer> comparedRowsList; //to avoid comparing the same row twice


    public void initComparedRows() {
        comparedRowsList = new ArrayList<>();
    }

    /**
     * Same as DBUnitAssert with support for regex in row values
     *
     * @param expectedTable  expected table
     * @param actualTable    current table
     * @param comparisonCols columnName
     * @param failureHandler handler
     * @throws DataSetException if datasets does not match
     */
    @Override
    protected void compareData(ITable expectedTable, ITable actualTable, ComparisonColumn[] comparisonCols, FailureHandler failureHandler) throws DataSetException {
        logger.debug("compareData(expectedTable={}, actualTable={}, "
                        + "comparisonCols={}, failureHandler={}) - start",
                new Object[]{expectedTable, actualTable, comparisonCols,
                        failureHandler});

        if (expectedTable == null) {
            throw new NullPointerException(
                    "The parameter 'expectedTable' must not be null");
        }
        if (actualTable == null) {
            throw new NullPointerException(
                    "The parameter 'actualTable' must not be null");
        }
        if (comparisonCols == null) {
            throw new NullPointerException(
                    "The parameter 'comparisonCols' must not be null");
        }
        if (failureHandler == null) {
            throw new NullPointerException(
                    "The parameter 'failureHandler' must not be null");
        }

        // iterate over all rows
        for (int i = 0; i < expectedTable.getRowCount(); i++) {
            if (skipRow(i)) {
                continue;
            }
            comparedRowsList.add(i);
            // iterate over all columns of the current row
            for (int j = 0; j < comparisonCols.length; j++) {
                ComparisonColumn compareColumn = comparisonCols[j];

                String columnName = compareColumn.getColumnName();
                DataType dataType = compareColumn.getDataType();

                Object expectedValue = expectedTable.getValue(i, columnName);
                Object actualValue = actualTable.getValue(i, columnName);

                if (skipCompare(columnName, expectedValue, actualValue)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("ignoring comparison " + expectedValue + "=" +
                                actualValue + " on column " + columnName);
                    }
                    continue;
                }
                if (expectedValue != null) {
                    if (expectedValue.toString().startsWith("regex:")) {
                        if (!regexMatches(expectedValue.toString(), actualValue)) {
                            Difference diff = new Difference(
                                    expectedTable, actualTable,
                                    i, columnName,
                                    expectedValue, actualValue);

                            // Handle the difference (throw error immediately or something else)
                            failureHandler.handle(diff);
                        }
                        continue;
                    }
                    if (manager.rowValueContainsScriptEngine(expectedValue)) {
                        try {
                            if (manager.hasScriptExpression(expectedValue) && !manager.getScriptAssert(expectedValue.toString(), actualValue)) {
                                Difference diff = new Difference(
                                        expectedTable, actualTable,
                                        i, columnName,
                                        expectedValue, actualValue);

                                // Handle the difference (throw error immediately or something else)
                                failureHandler.handle(diff);
                            }
                            continue;
                        } catch (Exception e) {
                            logger.warn(String.format("Could not evaluate script expression '%s' for table '%s', column '%s'.", expectedValue, actualTable.getTableMetaData().getTableName(), columnName), e);
                        }
                    }
                }
                if (dataType.compare(expectedValue, actualValue) != 0) {
                    Difference diff = new Difference(
                            expectedTable, actualTable,
                            i, columnName,
                            expectedValue, actualValue);

                    // Handle the difference (throw error immediately or something else)
                    failureHandler.handle(diff);
                }
            }
        }
    }

    private boolean skipRow(int i) {
        return comparedRowsList.contains(i);
    }

    @Override
    protected void compareData(ITable expectedTable, ITable actualTable, ComparisonColumn[] comparisonCols, FailureHandler failureHandler, ValueComparer defaultValueComparer,
                               final Map<String, ValueComparer> columnValueComparers,
                               final int rowNum, final int columnNum) throws DataSetException {
        this.compareData(expectedTable, actualTable, comparisonCols, failureHandler);
    }


    private boolean regexMatches(String expectedValue, Object actualValue) {
        if (actualValue == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(expectedValue.substring(expectedValue.indexOf(':') + 1).trim());
        return pattern.matcher(actualValue.toString()).matches();
    }
}
