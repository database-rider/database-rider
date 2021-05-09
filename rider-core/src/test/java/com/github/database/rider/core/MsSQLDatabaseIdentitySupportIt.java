package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.configuration.DataSetConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.testcontainers.containers.MSSQLServerContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RunWith(JUnit4.class)
@DBUnit(caseSensitiveTableNames = true, escapePattern = "\"?\"")
public class MsSQLDatabaseIdentitySupportIt {
    private static MSSQLServerContainer<?> mssqlserver;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(getConnection());

    private DataSetConfig identityDataSet = new DataSetConfig("datasets/xml/user.xml")
            .executeScripsBefore(new String[] {"mssql/userTableWithIdentityColumn.sql"})
            .executeStatementsAfter(new String[] {"DROP TABLE IF EXISTS [USER];"});

    @BeforeClass
    public static void setupContainer() {
        mssqlserver = new MSSQLServerContainer <>();
        mssqlserver.start();
    }

    @AfterClass
    public static void shutdown() {
        mssqlserver.stop();
    }

    @Test(expected = RuntimeException.class)
    public void identityColumnsCouldNotBePopulatedDirectly() {
        dbUnitRule.getDataSetExecutor().createDataSet(identityDataSet);
    }

    @Test
    @ExpectedDataSet("datasets/xml/user.xml")
    public void identityColumnsCouldBePopulatedWithSpecialMode() {
        identityDataSet.fillIdentityColumns(true);
        dbUnitRule.getDataSetExecutor().createDataSet(identityDataSet);
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:tc:sqlserver:2017-CU12://localhost:1433;databaseName=tempdb", "SA", "A_Str0ng_Required_Password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
