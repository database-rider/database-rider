package com.github.database.rider.core.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by pestano on 03/09/16.
 * pojo which represents dbunit.yml, used for global which can be overrided via @DataSet annotation
 * at class or method level and with @DBUnit at class or method level
 */
public class GlobalConfig {

    private static GlobalConfig instance;

    private GlobalConfig() {
    }


    private DBUnitConfig dbUnitConfig;

    public static GlobalConfig instance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    public static GlobalConfig newInstance() {
        instance = null;
        return instance();
    }

    private static void createInstance() {
        instance = new GlobalConfig();
        DBUnitConfig dbUnitConfig = null;
        DBUnitConfig defaultConfig = new Yaml().loadAs(GlobalConfig.class.getResourceAsStream("/default/dbunit.yml"), DBUnitConfig.class);
        //try to instance user provided dbunit.yml
        InputStream customConfiguration = Thread.currentThread().getContextClassLoader().getResourceAsStream("dbunit.yml");
        if (customConfiguration != null) {
            dbUnitConfig = new Yaml().loadAs(customConfiguration, DBUnitConfig.class);
        }

        if (dbUnitConfig == null) {
            dbUnitConfig = defaultConfig;

        } else { //merge default and user defined
            if (dbUnitConfig.isCacheConnection() == null) {
                dbUnitConfig.cacheConnection(defaultConfig.isCacheConnection());
            }

            if (dbUnitConfig.isCacheTableNames() == null) {
                dbUnitConfig.cacheTableNames(defaultConfig.isCacheTableNames());
            }

            if (dbUnitConfig.isLeakHunter() == null) {
                dbUnitConfig.leakHunter(defaultConfig.isLeakHunter());
            }

            if (dbUnitConfig.isLeakHunter() == null) {
                dbUnitConfig.leakHunter(defaultConfig.isLeakHunter());
            }

            if (dbUnitConfig.getProperties() == null || dbUnitConfig.getProperties().isEmpty()) {
                dbUnitConfig.setProperties(defaultConfig.getProperties());
            } else {
                Map<String, Object> properties = dbUnitConfig.getProperties();
                if (!properties.containsKey("batchedStatements")) {
                    properties.put("batchedStatements", defaultConfig.getProperties().get("batchedStatements"));
                }
                if (!properties.containsKey("qualifiedTableNames")) {
                    properties.put("qualifiedTableNames", defaultConfig.getProperties().get("qualifiedTableNames"));
                }
                if (!properties.containsKey("caseSensitiveTableNames")) {
                    properties.put("caseSensitiveTableNames", defaultConfig.getProperties().get("caseSensitiveTableNames"));
                }
                if (!properties.containsKey("batchSize")) {
                    properties.put("batchSize", defaultConfig.getProperties().get("batchSize"));
                }
                if (!properties.containsKey("fetchSize")) {
                    properties.put("fetchSize", defaultConfig.getProperties().get("fetchSize"));
                }
                if (!properties.containsKey("allowEmptyFields")) {
                    properties.put("allowEmptyFields", defaultConfig.getProperties().get("allowEmptyFields"));
                }
                if (!properties.containsKey("escapePattern")) {
                    properties.put("escapePattern", defaultConfig.getProperties().get("escapePattern"));
                }

            }

        }

        if (dbUnitConfig.getProperties().containsKey("escapePattern") && dbUnitConfig.getProperties().get("escapePattern").equals("")) {
            //avoid Caused by: org.dbunit.DatabaseUnitRuntimeException: Empty string is an invalid escape pattern!
            // because @DBUnit annotation and dbunit.yml global config have escapePattern defaults to ""
            dbUnitConfig.getProperties().remove("escapePattern");
        }
        instance.dbUnitConfig = dbUnitConfig;

    }

    public DBUnitConfig getDbUnitConfig() {
        return dbUnitConfig;
    }

}
