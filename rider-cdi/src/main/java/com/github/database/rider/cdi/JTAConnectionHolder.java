package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.RiderPUAnnotation;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class JTAConnectionHolder {

    private static final Logger log = LoggerFactory.getLogger(JTAConnectionHolder.class.getName());

    protected Map<String, Connection> connections = new HashMap<>();

    @Inject
    @Any
    protected Instance<DataSource> datasources;

    public void init(String dataSourceName) {
        try {
            DataSource dataSource = resolveDataSource(dataSourceName);
            if (!connections.containsKey(dataSourceName) || !isCachedConnection()) {
                connections.put(dataSourceName, dataSource.getConnection());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not acquire sql connection", e);
        }
    }

    private DataSource resolveDataSource(String dataSourceName) {
        if ("".equals(dataSourceName)) { //default datasource
            return CDI.current().select(DataSource.class).get();
        } else {
            return datasources.select(DataSource.class, new RiderPUAnnotation(dataSourceName)).get();
        }
    }

    public Connection getConnection(String datasourceBeanName) {
        if (!isCachedConnection()) {
            this.init(datasourceBeanName);
        }
        return connections.get(datasourceBeanName);
    }

    public void tearDown(String dataSource) {
        if (!isCachedConnection()) {
            try {
                connections.get(dataSource).close();
            } catch (SQLException e) {
                log.error("Could not close sql connection", e);
            }
        }
    }

    private boolean isCachedConnection() {
        DataSetExecutorImpl cdiDataSetExecutor = DataSetExecutorImpl.getExecutorById(DataSetProcessor.CDI_DBUNIT_EXECUTOR);
        return cdiDataSetExecutor != null && cdiDataSetExecutor.getDBUnitConfig().isCacheConnection();
    }
}
