package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * represents DBUnit configuration of a dataset executor.
 */
public class DBUnitConfig {

    private String executorId;

    private Boolean cacheConnection;

    private Boolean cacheTableNames;

    private Boolean leakHunter;

    private Map<String, Object> properties;

    private ConnectionConfig connectionConfig = new ConnectionConfig();

    public DBUnitConfig() {
        this.executorId = DataSetExecutorImpl.DEFAULT_EXECUTOR_ID;
        cacheConnection = false;
        cacheTableNames = false;
        leakHunter = false;
    }

    public DBUnitConfig(String executor) {
        properties = new HashMap<>();
        this.executorId = executor;
        if ("".equals(this.executorId)) {
            this.executorId = DataSetExecutorImpl.DEFAULT_EXECUTOR_ID;
        }
    }


    public static DBUnitConfig from(DBUnit dbUnit) {
        DBUnitConfig dbUnitConfig = new DBUnitConfig(dbUnit.executor());

        dbUnitConfig.cacheConnection(dbUnit.cacheConnection()).
                cacheTableNames(dbUnit.cacheTableNames()).
                leakHunter(dbUnit.leakHunter()).
                addDBUnitProperty("batchedStatements", dbUnit.batchedStatements()).
                addDBUnitProperty("batchSize", dbUnit.batchSize()).
                addDBUnitProperty("allowEmptyFields", dbUnit.allowEmptyFields()).
                addDBUnitProperty("fetchSize", dbUnit.fetchSize()).
                addDBUnitProperty("caseSensitiveTableNames",dbUnit.caseSensitiveTableNames()).
                addDBUnitProperty("qualifiedTableNames", dbUnit.qualifiedTableNames());

        if (!"".equals(dbUnit.escapePattern())) {
            dbUnitConfig.addDBUnitProperty("escapePattern", dbUnit.escapePattern());
        }

        //declarative connection config
        dbUnitConfig.driver(dbUnit.driver()).
                url(dbUnit.url()).
                    user(dbUnit.user()).
                password(dbUnit.password());

        return dbUnitConfig;
    }

    public static DBUnitConfig fromGlobalConfig() {
        return GlobalConfig.instance().getDbUnitConfig();
    }

    public static DBUnitConfig from(Method method) {
        DBUnit dbUnitConfig = method.getAnnotation(DBUnit.class);
        if (dbUnitConfig == null) {
            dbUnitConfig = method.getDeclaringClass().getAnnotation(DBUnit.class);
        }
        if (dbUnitConfig != null) {
            return from(dbUnitConfig);
        } else {
            return fromGlobalConfig();
        }
    }


    public DBUnitConfig cacheConnection(boolean cacheConnection) {
        this.cacheConnection = cacheConnection;
        return this;
    }

    public DBUnitConfig executorId(String executorId){
        this.executorId = executorId;
        return this;
    }

    public DBUnitConfig leakHunter(boolean leakHunter){
        this.leakHunter = leakHunter;
        return this;
    }


    public DBUnitConfig cacheTableNames(boolean cacheTables) {
        this.cacheTableNames = cacheTables;
        return this;
    }

    public DBUnitConfig addDBUnitProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public DBUnitConfig driver(String driverClass){
        connectionConfig.setDriver(driverClass);
        return this;
    }

    public DBUnitConfig url(String url){
        connectionConfig.setUrl(url);
        return this;
    }

    public DBUnitConfig user(String user){
        connectionConfig.setUser(user);
        return this;
    }

    public DBUnitConfig password(String password){
        connectionConfig.setPassword(password);
        return this;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    //methods above are for snakeyml library

    public void setCacheConnection(boolean cacheConnection) {
        this.cacheConnection = cacheConnection;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setCacheTableNames(boolean cacheTableNames) {
        this.cacheTableNames = cacheTableNames;
    }

    public Boolean isCacheConnection() {
        return cacheConnection;
    }

    public Boolean isCacheTableNames() {
        return cacheTableNames;
    }

    public Boolean isLeakHunter() {
        return leakHunter;
    }

    public void setLeakHunter(boolean activateLeakHunter) {
        this.leakHunter = activateLeakHunter;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}
