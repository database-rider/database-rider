package com.github.database.rider.core.configuration;

/**
 * Created by pestano on 03/09/16. pojo which represents dbunit.yml, used for global which can be overrided via @DataSet
 * annotation at class or method level and with @DBUnit at class or method level
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
        DBUnitConfig dbUnitConfig = DBUnitConfig.fromCustomGlobalFile();

        if (dbUnitConfig.getProperties().containsKey("escapePattern")
                && dbUnitConfig.getProperties().get("escapePattern").equals("")) {
            // avoid Caused by: org.dbunit.DatabaseUnitRuntimeException: Empty string is an invalid escape pattern!
            // because @DBUnit annotation and dbunit.yml global config have escapePattern defaults to ""
            dbUnitConfig.getProperties().remove("escapePattern");
        }
        instance.dbUnitConfig = dbUnitConfig;

    }

    public DBUnitConfig getDbUnitConfig() {
        return dbUnitConfig;
    }

}
