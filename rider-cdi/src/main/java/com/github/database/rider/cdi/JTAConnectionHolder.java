package com.github.database.rider.cdi;

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
		if(connection != null) {
			return;
		}
		try {
			connection = CDI.current().select(DataSource.class).get().getConnection();
		} catch (SQLException e) {
			log.error("Could not acquire sql connection", e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Execute after test, by default we do nothing but can be extended in order to optimize connection handling via connection pool
	 */
	public void tearDown() {
		//no-op
	}
}
