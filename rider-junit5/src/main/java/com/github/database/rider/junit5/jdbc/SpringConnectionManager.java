package com.github.database.rider.junit5.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.connection.ConnectionHolderImpl;

public final class SpringConnectionManager {

	private static final Lock LOCK = new ReentrantLock();

    public static ConnectionHolder getConnectionHolder(String executorId, DataSource dataSource) {
    	return new ConnectionHolderImpl(getReleaseConnectionWithoutClose(dataSource));
    }

    private static Connection getReleaseConnectionWithoutClose(final DataSource dataSource) {
    	final Connection origin = DataSourceUtils.getConnection(dataSource);;
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[] { Connection.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						LOCK.lock();
						try {
							if ("close".equals(method.getName())) {
								DataSourceUtils.releaseConnection(origin, dataSource);
								return Void.TYPE;
							}
							return method.invoke(origin, args);
						} finally {
							LOCK.unlock();
						}
					}
				}
		);
    }
}
