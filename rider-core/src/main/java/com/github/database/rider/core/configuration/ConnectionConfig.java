package com.github.database.rider.core.configuration;

import static com.github.database.rider.core.configuration.DBUnitConfigPropertyResolver.resolveProperty;

/**
 * Created by rafael-pestano on 13/09/2016.
 */
public class ConnectionConfig {

    private String driver; //needed by jdbc 3 or below drivers
    private String url;
    private String user;
    private String password;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = resolveProperty(driver);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = resolveProperty(url);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = resolveProperty(user);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = resolveProperty(password);
    }
}
