package com.github.database.rider.core.assertion;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.ITable;

/**
 * Created by rmpestano on 5/28/16.
 */
public class DataSetAssertion {

    private static final DataSetAssert INSTANCE = new DataSetAssert();

    public static void assertEqualsIgnoreCols(ITable expectedDataSet,
                                    ITable actualDataSet, String[] ignoreCols)
            throws DatabaseUnitException {
        expectedDataSet = OrderedByPkTable.create(expectedDataSet, actualDataSet);
        INSTANCE.initComparedRows();
        INSTANCE.assertEqualsIgnoreCols(expectedDataSet, actualDataSet, ignoreCols);
    }


}
