package com.github.database.rider.core.dataset.builder;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.DataSetBuilder;

public class RiderDataSetBuilder extends DataSetBuilder {

    public RiderDataSetBuilder() throws DataSetException {
        super(true);
    }

    @Override
    public RiderDataRowBuilder newRow(String tableName) {
        return new RiderDataRowBuilder(this, tableName);
    }

}
