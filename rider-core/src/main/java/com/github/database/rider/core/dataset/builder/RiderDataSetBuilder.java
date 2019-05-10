package com.github.database.rider.core.dataset.builder;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.DataSetBuilder;

/**
 * @author rmpestano
 */
public class RiderDataSetBuilder extends DataSetBuilder {

    boolean uppercase;

    public RiderDataSetBuilder() throws DataSetException {
        super(false);
        this.uppercase = false;
    }

    public RiderDataSetBuilder(boolean uppercase) throws DataSetException {
        super(uppercase);
        this.uppercase = uppercase;
    }

    @Override
    public RiderDataRowBuilder newRow(String tableName) {
        return new RiderDataRowBuilder(this, uppercase ? tableName.toUpperCase() : tableName, uppercase);
    }

}
