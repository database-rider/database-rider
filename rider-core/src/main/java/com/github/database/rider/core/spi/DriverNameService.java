package com.github.database.rider.core.spi;

import java.sql.Connection;

/**
 * Used by DBRider to know which driver it is working on.
 *
 * By default DBRider will try to infer the name from connection metadata but if your driver name
 * does not match expected names you can implement this interface and enable it
 * in src/main/resources/META-INF/services/com.github.database.rider.core.spi.DriverNameService file
 * which may contain the fully qualified name of your DriverNameService implementation.
 *
 * @see com.github.database.rider.core.util.DriverUtils#getDriverName(Connection)
 */
public interface DriverNameService {

    String getDriverName(java.sql.Connection connection);
}
