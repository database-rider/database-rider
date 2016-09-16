package com.github.database.rider.configuration;

import com.github.database.rider.api.configuration.DBUnit;
import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.api.dataset.SeedStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by pestano on 03/09/16.
 */
@RunWith(JUnit4.class)
public class ConfigurationTest {


    @Test
    public void shouldLoadDBUnitConfigViaGlobalFile(){
        GlobalConfig globaConfig = GlobalConfig.newInstance();
        assertThat(globaConfig).isNotNull()
        .extracting("dbUnitConfig.cacheConnection","dbUnitConfig.cacheTableNames","dbUnitConfig.leakHunter")
        .contains(true,true,false);

        assertThat(globaConfig.getDbUnitConfig().getProperties()).
                containsEntry("allowEmptyFields", false).
                containsEntry("batchedStatements", false).
                containsEntry("qualifiedTableNames", false).
                containsEntry("caseSensitiveTableNames", false).
                containsEntry("batchSize", 100).
                containsEntry("fetchSize",100).
                doesNotContainKey("escapePattern");
        assertThat(globaConfig.getDbUnitConfig().getConnectionConfig()).extracting("driver", "url", "user","password").
                contains("", "", "", "");
    }

    @Test
    public void shouldLoadDBUnitConfigViaCustomGlobalFile() throws IOException {
    	File backupConfig = new File("target/test-classes/dbunit-backup.yml");
    	File customConfig = new File("target/test-classes/dbunit.yml");
        FileOutputStream backupStream = new FileOutputStream(backupConfig);
        backupStream.write(Files.readAllBytes(Paths.get(getClass().getResource("/default/dbunit.yml").getPath().replaceFirst("^/(.:/)", "$1"))));

        backupStream.flush();
        backupStream.close();
        FileOutputStream fos = new FileOutputStream(customConfig);
        fos.write(Files.readAllBytes(Paths.get(getClass().getResource("/config/sample-dbunit.yml").getPath().replaceFirst("^/(.:/)", "$1"))));
        fos.flush();
        fos.close();

        GlobalConfig globaConfig = GlobalConfig.newInstance();
        assertThat(globaConfig).isNotNull()
                .extracting("dbUnitConfig.cacheConnection","dbUnitConfig.cacheTableNames","dbUnitConfig.leakHunter")
                .contains(false,false,true);

        assertThat(globaConfig.getDbUnitConfig().getProperties()).
                containsEntry("allowEmptyFields", true).
                containsEntry("batchedStatements", true).
                containsEntry("qualifiedTableNames", true).
                containsEntry("batchSize", 200).
                containsEntry("fetchSize",200).
                containsEntry("escapePattern","[?]");

        FileOutputStream originalStream = new FileOutputStream(customConfig);
        originalStream.write(Files.readAllBytes(Paths.get(backupConfig.toURI())));
        originalStream.flush();
        originalStream.close();
        
    }



    @Test
    @DataSet(strategy = SeedStrategy.UPDATE,disableConstraints = true,cleanAfter = true,transactional = true)
    public void shouldLoadDataSetConfigFromAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDataSetConfigFromAnnotation");
        assertThat(method).isNotNull();
        DataSet dataSet = method.getAnnotation(DataSet.class);
        assertThat(dataSet).isNotNull();

        DataSetConfig dataSetConfig = new DataSetConfig().from(dataSet);
        assertThat(dataSetConfig).isNotNull().
                extracting("strategy","useSequenceFiltering","disableConstraints","cleanBefore","cleanAfter","transactional").
                contains(SeedStrategy.UPDATE, true, true, false,true,true);

    }


    @Test
    @DBUnit(cacheConnection = true, cacheTableNames = false, allowEmptyFields = true,batchSize = 50)
    public void shouldLoadDBUnitConfigViaAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("shouldLoadDBUnitConfigViaAnnotation");
        assertThat(method).isNotNull();
        DBUnit dbUnit = method.getAnnotation(DBUnit.class);
        assertThat(dbUnit).isNotNull();
        DBUnitConfig dbUnitConfig = DBUnitConfig.from(dbUnit);
        assertThat(dbUnitConfig).isNotNull()
                    .extracting("cacheConnection","cacheTableNames")
                    .contains(true,false);

        assertThat(dbUnitConfig.getProperties()).
                    containsEntry("allowEmptyFields", true).
                    containsEntry("batchedStatements", false).
                    containsEntry("qualifiedTableNames", false).
                    containsEntry("batchSize", 50).
                    containsEntry("fetchSize",100).
                    doesNotContainKey("escapePattern");
    }


}
