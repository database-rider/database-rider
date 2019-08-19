package com.github.database.rider.core.spi;

/**
 * Used to the DBRider which driver it is working on.
 *
 * By default DBRider will try to infer the name from connection metadata but if your driver name
 * does not match expected names you can implement his interface and enable it
 * in src/main/resources/META-INF/services/com.github.database.rider.core.spi.DriverNameService file
 * which may contain the fully qualified name of your DriverNameService implementation.
 */
public interface DriverNameService {

    String getDriverName(java.sql.Connection connection);
}
