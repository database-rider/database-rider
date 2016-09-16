package com.github.database.rider.api.dataset;

/**
 * Created by pestano on 23/07/15.
 */

import org.dbunit.operation.DatabaseOperation;

/**
 Same as arquillian persistence: https://docs.jboss.org/author/display/ARQ/Persistence
 Data insert strategies
 DBUnit, and hence Arquillian Persistence Extension, provides following strategies for inserting data

 INSERT
 Performs insert of the data defined in provided data sets. This is the default strategy.

 CLEAN_INSERT
 Performs insert of the data defined in provided data sets, after removal of all data present in the tables (DELETE_ALL invoked by DBUnit before INSERT).

 REFRESH
 During this operation existing rows are updated and new ones are inserted. Entries already existing in the database which are not defined in the provided data set are not affected.

 UPDATE
 This strategy updates existing rows using data provided in the datasets. If dataset contain a row which is not present in the database (identified by its primary key) then exception is thrown.
 */
public enum SeedStrategy {
    CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT),
    INSERT(DatabaseOperation.INSERT),
    REFRESH(DatabaseOperation.REFRESH),
    UPDATE(DatabaseOperation.UPDATE);

    private final DatabaseOperation operation;

    SeedStrategy(DatabaseOperation operation) {
        this.operation = operation;
    }

    public DatabaseOperation getOperation() {
        return operation;
    }
}
