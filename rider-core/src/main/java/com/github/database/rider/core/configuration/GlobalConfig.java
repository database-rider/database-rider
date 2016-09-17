package com.github.database.rider.core.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Created by pestano on 03/09/16.
 *
 * pojo which represents dbunit.yml, used for global which can be overrided via @DataSet annotation
 * at class or method level and with @DBUnit at class or method level
 */
public class GlobalConfig {

    private static GlobalConfig instance;

    private DBUnitConfig dbUnitConfig;


    private GlobalConfig() {
    }

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
        //try to instance user provided dbunit.yml
        InputStream customConfiguration = Thread.currentThread().getContextClassLoader().getResourceAsStream("dbunit.yml");
        if (customConfiguration != null) {
            dbUnitConfig = new Yaml().loadAs(customConfiguration, DBUnitConfig.class);
        } 
        
        if(dbUnitConfig == null) { 
        	//default config
        	dbUnitConfig = new Yaml().loadAs(GlobalConfig.class.getResourceAsStream("/default/dbunit.yml"), DBUnitConfig.class);
        	
        }

        if (dbUnitConfig.getProperties().containsKey("escapePattern")) {
            if (dbUnitConfig.getProperties().get("escapePattern").equals("")) {
                //avoid Caused by: org.dbunit.DatabaseUnitRuntimeException: Empty string is an invalid escape pattern!
                // because @DBUnit annotation and dbunit.yml global config have escapePattern defaults to ""
                dbUnitConfig.getProperties().remove("escapePattern");
            }
        }
        instance.dbUnitConfig = dbUnitConfig;

    }

    public DBUnitConfig getDbUnitConfig() {
        return dbUnitConfig;
    }

}
