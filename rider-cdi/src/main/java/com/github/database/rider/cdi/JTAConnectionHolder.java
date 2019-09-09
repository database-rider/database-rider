package com.github.database.rider.cdi;

import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
public class JTAConnectionHolder {

	private static final Logger log = LoggerFactory.getLogger(JTAConnectionHolder.class.getName());

	protected Connection connection;

	@PostConstruct
	public void init() {
		try {
			connection = CDI.current().select(DataSource.class).get().getConnection();
		} catch (SQLException e) {
			log.error("Could not acquire sql connection", e);
		}
	}

	public Connection getConnection() {
		if(!isCachedConnection()) {
			this.init();
		}
		return connection;
	}

	public void tearDown() {
		if(!isCachedConnection()) {
			try {
				connection.close();
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
