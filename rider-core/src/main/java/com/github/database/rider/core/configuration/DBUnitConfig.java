package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.replacers.DateTimeReplacer;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.replacers.Replacer;
import com.github.database.rider.core.replacers.UnixTimestampReplacer;

import org.dbunit.database.IMetadataHandler;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * represents DBUnit configuration of a dataset executor.
 */
public class DBUnitConfig {

    private String executorId;
    private Boolean cacheConnection;
    private Boolean cacheTableNames;
    private Boolean leakHunter;
    private Boolean mergeDataSets;
    private Boolean columnSensing;
    private Orthography caseInsensitiveStrategy;
    private Map<String, Object> properties;
    private ConnectionConfig connectionConfig;

    public DBUnitConfig() {
        this(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID);
    }

    public DBUnitConfig(String executor) {
        this.executorId = executor;
        initDefault();
    }

    private void initDefault() {
        if ("".equals(executorId)) {
            executorId = DataSetExecutorImpl.DEFAULT_EXECUTOR_ID;
        }

        cacheConnection = true;
        cacheTableNames = true;
        leakHunter = false;
        caseInsensitiveStrategy = Orthography.UPPERCASE;
        mergeDataSets = Boolean.FALSE;
        columnSensing = Boolean.FALSE;

        initDefaultProperties();
        initDefaultConnectionConfig();
    }

    private void initDefaultProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }

        putIfAbsent(properties, "batchedStatements", false);
        putIfAbsent(properties, "qualifiedTableNames", false);
        putIfAbsent(properties, "schema", null);
        putIfAbsent(properties, "caseSensitiveTableNames", false);
        putIfAbsent(properties, "batchSize", 100);
        putIfAbsent(properties, "fetchSize", 100);
        putIfAbsent(properties, "allowEmptyFields", false);
        putIfAbsent(properties, "replacers", new ArrayList<>(
                Arrays.asList(new DateTimeReplacer(), new UnixTimestampReplacer(), new NullReplacer())));
    }

    private <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    private void initDefaultConnectionConfig() {
        if (connectionConfig == null) {
            connectionConfig = new ConnectionConfig();
        }

        if (connectionConfig.getDriver() == null) {
            connectionConfig.setDriver("");
        }

        if (connectionConfig.getUrl() == null) {
            connectionConfig.setUrl("");
        }

        if (connectionConfig.getUser() == null) {
            connectionConfig.setUser("");
        }

        if (connectionConfig.getPassword() == null) {
            connectionConfig.setPassword("");
        }
    }

    public static DBUnitConfig fromCustomGlobalFile() {
        try (InputStream customConfiguration = Thread.currentThread().getContextClassLoader().getResourceAsStream("dbunit.yml")) {
            if (customConfiguration != null) {
                DBUnitConfig configFromFile = new Yaml().loadAs(customConfiguration, DBUnitConfig.class);
                configFromFile.initDefaultProperties();
                configFromFile.initDefaultConnectionConfig();

                return configFromFile;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't load config from global file", e);
        }

        return new DBUnitConfig();
    }

    public static DBUnitConfig from(DBUnit dbUnit) {
        DBUnitConfig dbUnitConfig = new DBUnitConfig(dbUnit.executor());

        dbUnitConfig.cacheConnection(dbUnit.cacheConnection())
                .cacheTableNames(dbUnit.cacheTableNames())
                .leakHunter(dbUnit.leakHunter())
                .mergeDataSets(dbUnit.mergeDataSets())
                .columnSensing(dbUnit.columnSensing())
                .addDBUnitProperty("batchedStatements", dbUnit.batchedStatements())
                .addDBUnitProperty("batchSize", dbUnit.batchSize())
                .addDBUnitProperty("allowEmptyFields", dbUnit.allowEmptyFields())
                .addDBUnitProperty("fetchSize", dbUnit.fetchSize())
                .addDBUnitProperty("qualifiedTableNames", dbUnit.qualifiedTableNames())
                .addDBUnitProperty("schema",
                        dbUnit.schema() != null && !dbUnit.schema().isEmpty() ? dbUnit.schema() : null)
                .addDBUnitProperty("caseSensitiveTableNames", dbUnit.caseSensitiveTableNames())
                .caseInsensitiveStrategy(dbUnit.caseInsensitiveStrategy());

        if (!"".equals(dbUnit.escapePattern())) {
            dbUnitConfig.addDBUnitProperty("escapePattern", dbUnit.escapePattern());
        }

        if (!dbUnit.dataTypeFactoryClass().isInterface()) {
            try {
                IDataTypeFactory factory = dbUnit.dataTypeFactoryClass().newInstance();
                dbUnitConfig.addDBUnitProperty("datatypeFactory", factory);
            }
            catch (Exception e) {
                throw new RuntimeException("failed to instantiate datatypeFactory", e);
            }
        }

        if (!dbUnit.metaDataHandler().isInterface()) {
            try {
                IMetadataHandler factory = dbUnit.metaDataHandler().newInstance();
                dbUnitConfig.addDBUnitProperty("metadataHandler", factory);
            }
            catch (Exception e) {
                throw new RuntimeException("failed to instantiate metadataHandler", e);
            }
        }
        
        
        List<Replacer> dbUnitReplacers = new ArrayList<>();
        for (Class<? extends Replacer> replacerClass : dbUnit.replacers()) {
            try {
                dbUnitReplacers.add(replacerClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(replacerClass.getName() + " could not be instantiated as Replacer");
            }
        }

        @SuppressWarnings("unchecked") List<Replacer> defaultReplacers = (List<Replacer>) dbUnitConfig.getProperties().get("replacers");
        if (defaultReplacers != null && defaultReplacers.size() > 0) {
            // merge replacers
            dbUnitReplacers.addAll(defaultReplacers);
        }

        dbUnitConfig.addDBUnitProperty("replacers", dbUnitReplacers);


        // declarative connection config
        dbUnitConfig.driver(dbUnit.driver())
                .url(dbUnit.url())
                .user(dbUnit.user())
                .password(dbUnit.password());

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

    public DBUnitConfig executorId(String executorId) {
        this.executorId = executorId;
        return this;
    }

    public DBUnitConfig leakHunter(boolean leakHunter) {
        this.leakHunter = leakHunter;
        return this;
    }

    public DBUnitConfig cacheTableNames(boolean cacheTables) {
        this.cacheTableNames = cacheTables;
        return this;
    }
    
    public DBUnitConfig mergeDataSets(boolean mergeDataSets) {
        this.mergeDataSets = mergeDataSets;
        return this;
    }

    public DBUnitConfig columnSensing(boolean columnSensing) {
        this.columnSensing = columnSensing;
        return this;
    }

    public DBUnitConfig caseInsensitiveStrategy(Orthography orthography) {
        this.caseInsensitiveStrategy = orthography;
        return this;
    }

    public DBUnitConfig addDBUnitProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public DBUnitConfig driver(String driverClass) {
        connectionConfig.setDriver(driverClass);
        return this;
    }

    public DBUnitConfig url(String url) {
        connectionConfig.setUrl(url);
        return this;
    }

    public DBUnitConfig user(String user) {
        connectionConfig.setUser(user);
        return this;
    }

    public DBUnitConfig password(String password) {
        connectionConfig.setPassword(password);
        return this;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    // methods above are for snakeyml library

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

    public Boolean isMergeDataSets() {
        return mergeDataSets;
    }

    public Boolean isColumnSensing() {
        return columnSensing;
    }

    public Boolean isLeakHunter() {
        return leakHunter;
    }

    public void setLeakHunter(boolean activateLeakHunter) {
        this.leakHunter = activateLeakHunter;
    }

    public Orthography getCaseInsensitiveStrategy() {
        return caseInsensitiveStrategy;
    }

    public void setCaseInsensitiveStrategy(Orthography caseInsensitiveStrategy) {
        this.caseInsensitiveStrategy = caseInsensitiveStrategy;
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

    public void setMergeDataSets(Boolean mergeDataSets) {
        this.mergeDataSets = mergeDataSets;
    }

    public boolean isCaseSensitiveTableNames() {
        return properties.containsKey("caseSensitiveTableNames") && Boolean.parseBoolean(properties.get("caseSensitiveTableNames").toString());
    }

    public String getSchema() {
        return (String) properties.get("schema");
    }

}
