package com.github.quarkus.sample.infra;

import com.github.database.rider.cdi.JTAConnectionHolder;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
@Alternative
@Priority(1)
public class AgroalConnectionHolder extends JTAConnectionHolder {

    private static final Logger log = LoggerFactory.getLogger(AgroalConnectionHolder.class.getName());

    private DataSource agroalDS;

    @PostConstruct
    public void init() {
        if(agroalDS != null) {
            return;
        }
        super.init();
        AgroalDataSourceConfigurationSupplier poolConfig = new AgroalDataSourceConfigurationSupplier()
                .metricsEnabled(false)
                .connectionPoolConfiguration(cp -> cp
                        .maxSize(10)
                        .connectionFactoryConfiguration(cf -> {

                                    try {
                                        return cf.jdbcUrl(super.getConnection().getMetaData().getURL());
                                    } catch (SQLException e) {
                                        throw new RuntimeException("Could not create connection pool configuration", e);
                                    }
                                }

                        )
                );

        try {
            agroalDS = AgroalDataSource.from(poolConfig);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create datasource from connection pool configuration", e);
        }
    }


    @Override
    public Connection getConnection() {
        try {
            return agroalDS.getConnection();
        } catch (SQLException e) {
            log.warn("Using default JTA connection.");
            //fallback to connection without pool
            return super.getConnection();
        }
    }
}
