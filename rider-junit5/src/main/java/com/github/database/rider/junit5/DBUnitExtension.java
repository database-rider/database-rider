package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.expoter.DataSetExportConfig;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.expoter.ExportDataSet;
import com.github.database.rider.core.configuration.ConnectionConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.exporter.DataSetExporter;
import com.github.database.rider.core.leak.LeakHunterException;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.dbunit.DatabaseUnitException;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Optional;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;

/**
 * Created by pestano on 27/08/16.
 */
public class DBUnitExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
	
    private static final Logger log = LoggerFactory.getLogger(DBUnitExtension.class);

	private static final Namespace namespace = Namespace.create(DBUnitExtension.class);

    @Override
    public void beforeTestExecution(TestExtensionContext testExtensionContext) throws Exception {

        if (!shouldCreateDataSet(testExtensionContext)) {
            return;
        }

        ConnectionHolder connectionHolder = findTestConnection(testExtensionContext);

        if (EntityManagerProvider.isEntityManagerActive()) {
            EntityManagerProvider.em().clear();
        }


        DataSet annotation = testExtensionContext.getTestMethod().get().getAnnotation(DataSet.class);
        if (annotation == null) {
            //try to infer from class level annotation
            annotation = testExtensionContext.getTestClass().get().getAnnotation(DataSet.class);
        }


        DBUnitConfig dbUnitConfig = DBUnitConfig.from(testExtensionContext.getTestMethod().get());
        final DataSetConfig dataSetConfig = new DataSetConfig().from(annotation);
        if(connectionHolder == null || connectionHolder.getConnection() == null){
            connectionHolder = createConnection(dbUnitConfig,testExtensionContext.getTestMethod().get().getName());
        }
        DataSetExecutor executor = DataSetExecutorImpl.instance(dataSetConfig.getExecutorId(), connectionHolder);
        executor.setDBUnitConfig(dbUnitConfig);
        DBUnitTestContext dbUnitTestContext = getTestContext(testExtensionContext);
        dbUnitTestContext.setExecutor(executor).
        setDataSetConfig(dataSetConfig);


        if (dataSetConfig != null && dataSetConfig.getExecuteStatementsBefore() != null && dataSetConfig.getExecuteStatementsBefore().length > 0) {
            try {
                executor.executeStatements(dataSetConfig.getExecuteStatementsBefore());
            } catch (Exception e) {
                log.error(testExtensionContext.getTestMethod().get().getName() + "() - Could not execute statements Before:" + e.getMessage(), e);
            }
        }//end execute statements

        if (dataSetConfig.getExecuteScriptsBefore() != null && dataSetConfig.getExecuteScriptsBefore().length > 0) {
            try {
                for (int i = 0; i < dataSetConfig.getExecuteScriptsBefore().length; i++) {
                    executor.executeScript(dataSetConfig.getExecuteScriptsBefore()[i]);
                }
            } catch (Exception e) {
                if (e instanceof DatabaseUnitException) {
                    throw e;
                }
                log.error(testExtensionContext.getTestMethod().get().getName() + "() - Could not execute scriptsBefore:" + e.getMessage(), e);
            }
        }//end execute scripts
        
        if (dbUnitConfig.isLeakHunter()) {
            LeakHunter leakHunter = LeakHunterFactory.from(connectionHolder.getConnection());
            dbUnitTestContext.setLeakHunter(leakHunter).
            	setOpenConnections(leakHunter.openConnections());
        }

        try {
            executor.createDataSet(dataSetConfig);
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Could not create dataset for test method %s due to following error " + e.getMessage(), testExtensionContext.getTestMethod().get().getName()), e);
        }

        boolean isTransactional = dataSetConfig.isTransactional();
        if (isTransactional) {
            if (EntityManagerProvider.isEntityManagerActive()) {
                if(!EntityManagerProvider.tx().isActive()){
                    EntityManagerProvider.em().getTransaction().begin();
                }
            } else{
                Connection connection = executor.getConnectionHolder().getConnection();
                connection.setAutoCommit(false);
            }
        }

    }


    private boolean shouldCreateDataSet(TestExtensionContext testExtensionContext) {
        return testExtensionContext.getTestMethod().get().isAnnotationPresent(DataSet.class) || testExtensionContext.getTestClass().get().isAnnotationPresent(DataSet.class);
    }

    private boolean shouldCompareDataSet(TestExtensionContext testExtensionContext) {
        return testExtensionContext.getTestMethod().get().isAnnotationPresent(ExpectedDataSet.class) || testExtensionContext.getTestClass().get().isAnnotationPresent(ExpectedDataSet.class);
    }

    private boolean shouldExportDataSet(TestExtensionContext testExtensionContext) {
        return testExtensionContext.getTestMethod().get().isAnnotationPresent(ExportDataSet.class) || testExtensionContext.getTestClass().get().isAnnotationPresent(ExportDataSet.class);
    }

    public void exportDataSet(DataSetExecutor dataSetExecutor, Method method) {
        ExportDataSet exportDataSet = resolveExportDataSet(method);
        if(exportDataSet != null){
            DataSetExportConfig exportConfig = DataSetExportConfig.from(exportDataSet);
            String outputName = exportConfig.getOutputFileName();
            if(outputName == null || "".equals(outputName.trim())){
                outputName = method.getName().toLowerCase()+"."+exportConfig.getDataSetFormat().name().toLowerCase();
            }
            exportConfig.outputFileName(outputName);
            try {
                DataSetExporter.getInstance().export(dataSetExecutor.getDBUnitConnection(),exportConfig);
            } catch (Exception e) {
            	log.warn("Could not export dataset after method "+method.getName(),e);
            }
        }
    }

    private ExportDataSet resolveExportDataSet(Method method) {
        ExportDataSet exportDataSet = method.getAnnotation(ExportDataSet.class);
        if (exportDataSet == null) {
            exportDataSet = method.getDeclaringClass().getAnnotation(ExportDataSet.class);
        }
        return exportDataSet;
    }


    @Override
    public void afterTestExecution(TestExtensionContext testExtensionContext) throws Exception {
    	DBUnitTestContext dbUnitTestContext = getTestContext(testExtensionContext);
        DBUnitConfig dbUnitConfig = dbUnitTestContext.getExecutor().getDBUnitConfig();
        try {
            if (shouldCompareDataSet(testExtensionContext)) {
                ExpectedDataSet expectedDataSet = testExtensionContext.getTestMethod().get().getAnnotation(ExpectedDataSet.class);
                if (expectedDataSet == null) {
                    //try to infer from class level annotation
                    expectedDataSet = testExtensionContext.getTestClass().get().getAnnotation(ExpectedDataSet.class);
                }
                if (expectedDataSet != null) {
                    DataSetExecutor executor = dbUnitTestContext.getExecutor();
                    DataSetConfig datasetConfig = dbUnitTestContext.getDataSetConfig();
                    boolean isTransactional = datasetConfig.isTransactional();
                    if (isTransactional) {
                        try {
                            if (EntityManagerProvider.isEntityManagerActive()) {
                                if(EntityManagerProvider.tx().isActive()){
                                    EntityManagerProvider.tx().commit();
                                }
                            } else {
                                Connection connection = executor.getConnectionHolder().getConnection();
                                connection.commit();
                                connection.setAutoCommit(false);
                            }
                        }catch (Exception e){
                            if(EntityManagerProvider.isEntityManagerActive()){
                                EntityManagerProvider.tx().rollback();
                            } else{
                                Connection connection = executor.getConnectionHolder().getConnection();
                                connection.setAutoCommit(false);
                                connection.setReadOnly(true);
                            }
                        }
                    }
                    executor.compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value()).disableConstraints(true), expectedDataSet.ignoreCols());
                }
            }

            if (dbUnitConfig != null && dbUnitConfig.isLeakHunter()) {
                LeakHunter leakHunter = dbUnitTestContext.getLeakHunter();
                int openConnectionsBefore = dbUnitTestContext.getOpenConnections();
                int openConnectionsAfter = leakHunter.openConnections();
                if (openConnectionsAfter > openConnectionsBefore) {
                    throw new LeakHunterException(testExtensionContext.getTestMethod().get().getName(), openConnectionsAfter - openConnectionsBefore);
                }

            }

        } finally {

            DataSetConfig dataSetConfig = dbUnitTestContext.getDataSetConfig();
            if (dataSetConfig == null) {
                return;
            }
            DataSetExecutor executor = dbUnitTestContext.getExecutor();

            if(shouldExportDataSet(testExtensionContext)){
                exportDataSet(executor,testExtensionContext.getTestMethod().get());
            }

            if (dataSetConfig != null && dataSetConfig.getExecuteStatementsAfter() != null && dataSetConfig.getExecuteStatementsAfter().length > 0) {
                try {
                    executor.executeStatements(dataSetConfig.getExecuteStatementsAfter());
                } catch (Exception e) {
                    log.error(testExtensionContext.getTestMethod().get().getName() + "() - Could not execute statements after:" + e.getMessage(), e);
                }
            }//end execute statements

            if (dataSetConfig.getExecuteScriptsAfter() != null && dataSetConfig.getExecuteScriptsAfter().length > 0) {
                try {
                    for (int i = 0; i < dataSetConfig.getExecuteScriptsAfter().length; i++) {
                        executor.executeScript(dataSetConfig.getExecuteScriptsAfter()[i]);
                    }
                } catch (Exception e) {
                    if (e instanceof DatabaseUnitException) {
                        throw e;
                    }
                    log.error(testExtensionContext.getTestMethod().get().getName() + "() - Could not execute scriptsAfter:" + e.getMessage(), e);
                }
            }//end execute scripts

            if (dataSetConfig.isCleanAfter()) {
                executor.clearDatabase(dataSetConfig);
            }
        }

    }


    private ConnectionHolder findTestConnection(TestExtensionContext testExtensionContext) {
        Class<?> testClass = testExtensionContext.getTestClass().get();
        try {
            Optional<Field> fieldFound = Arrays.stream(testClass.getDeclaredFields()).
                    filter(f -> f.getType() == ConnectionHolder.class).
                    findFirst();

            if (fieldFound.isPresent()) {
                Field field = fieldFound.get();
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                ConnectionHolder connectionHolder = ConnectionHolder.class.cast(field.get(testExtensionContext.getTestInstance()));
                if (connectionHolder == null || connectionHolder.getConnection() == null) {
                    throw new RuntimeException("ConnectionHolder not initialized correctly");
                }
                return connectionHolder;
            }

            //try to get connection from method

            Optional<Method> methodFound = Arrays.stream(testClass.getDeclaredMethods()).
                    filter(m -> m.getReturnType() == ConnectionHolder.class).
                    findFirst();

            if (methodFound.isPresent()) {
                Method method = methodFound.get();
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                ConnectionHolder connectionHolder = ConnectionHolder.class.cast(method.invoke(testExtensionContext.getTestInstance()));
                if (connectionHolder == null || connectionHolder.getConnection() == null) {
                    throw new RuntimeException("ConnectionHolder not initialized correctly");
                }
                return connectionHolder;
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not get database connection for test " + testClass, e);
        }

        return null;


    }


    private ConnectionHolder createConnection(DBUnitConfig dbUnitConfig, String currentMethod) {
        ConnectionConfig connectionConfig = dbUnitConfig.getConnectionConfig();
        if ("".equals(connectionConfig.getUrl()) || "".equals(connectionConfig.getUser())) {
            throw new RuntimeException(String.format("Could not create JDBC connection for method %s, provide a connection at test level or via configuration, see documentation here: https://github.com/rmpestano/dbunit-rules#jdbc-connection", currentMethod));
        }

        try {
            if (!"".equals(connectionConfig.getDriver())) {
                Class.forName(connectionConfig.getDriver());
            }
            return new ConnectionHolderImpl(DriverManager.getConnection(connectionConfig.getUrl(), connectionConfig.getUser(), connectionConfig.getPassword()));
        } catch (Exception e) {
            log.error("Could not create JDBC connection for method " + currentMethod, e);
        }
        return null;
    }
    
    
    /**
     * one test context (datasetExecutor, dbunitConfig etc..) per test
     */
	private DBUnitTestContext getTestContext(ExtensionContext context) {
		Class<?> testClass = context.getTestClass().get();
		Store store = context.getStore(namespace);
		DBUnitTestContext testContext = store.get(testClass,DBUnitTestContext.class);
		if(testContext == null){
			testContext = new DBUnitTestContext();
			store.put(testClass, testContext);
		}
		return testContext;
	}

}
