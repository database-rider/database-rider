package com.github.quarkus.sample.infra;

import com.github.database.rider.cdi.JTAConnectionHolder;
import io.agroal.api.AgroalDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
@Alternative
@Priority(1)
public class AgroalConnectionHolder extends JTAConnectionHolder {

    private static final Logger log = LoggerFactory.getLogger(AgroalConnectionHolder.class.getName());

    private ThreadLocal<Connection> connection = new ThreadLocal<>();

    @Inject
    AgroalDataSource agroalDS;

    @Override
    public void init() {
    }

    @Override
    public Connection getConnection() {
        try {
             connection.set(agroalDS.getConnection());
        } catch (SQLException e) {
            log.warn("Using default JTA connection.");
            //fallback to connection without pool
            super.init();
            connection.set(super.getConnection());
        }
        return connection.get();
    }

    @Override
    public void tearDown() {
        try {
            connection.get().close();
        } catch (Exception e) {
            log.warn("Could not close current connection.", e);
        }
    }
}
