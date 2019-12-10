package com.github.quarkus.sample;

import com.github.database.rider.cdi.api.RiderPU;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class DataSourceProducer {

    @Inject
    AgroalDataSource defaultDataSource;//doesnt need to be produced

    @Inject
    @DataSource("secondary")
    AgroalDataSource secondaryDataSource;



    @Produces
    @RiderPU("secondary")
    public javax.sql.DataSource produceSecondary() {
        return secondaryDataSource;
    }


}
