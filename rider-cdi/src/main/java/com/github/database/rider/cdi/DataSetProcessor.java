package com.github.database.rider.cdi;

import com.github.database.rider.cdi.api.DefaultAnnotation;
import com.github.database.rider.cdi.api.RiderPUAnnotation;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.exporter.DataSetExportConfig;
import com.github.database.rider.core.api.exporter.ExportDataSet;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.exporter.DataSetExporter;
import com.github.database.rider.core.replacers.Replacer;
import org.dbunit.DatabaseUnitException;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by rafael-pestano on 08/10/2015.
 */
@RequestScoped
public class DataSetProcessor {

    public static final String CDI_DBUNIT_EXECUTOR = "CDI_DBUNIT_EXECUTOR";

    private static final Logger log = LoggerFactory.getLogger(DataSetProcessor.class.getName());

    private EntityManager entityManager;

    private Connection connection;

    private DataSetExecutor dataSetExecutor;

    private Boolean isJta;

    @Inject
    @Any
    Instance<EntityManager> entityManagerInstance;

    @Inject
    Instance<JTAConnectionHolder> jtaConnectionHolder;


    public void init(String entityManagerBeanName) {
        this.entityManager = resolveEntityManager(entityManagerBeanName);
        if (entityManager == null) {
            throw new RuntimeException("Please provide an entity manager via CDI producer, see examples here: https://deltaspike.apache.org/documentation/jpa.html");
        }
        entityManager.clear();
        this.connection = createConnection(entityManagerBeanName);
        final String executorName = CDI_DBUNIT_EXECUTOR + entityManagerBeanName;//one executor per entityManager
        dataSetExecutor = DataSetExecutorImpl.instance(executorName, new ConnectionHolderImpl(connection));
    }

    private EntityManager resolveEntityManager(String entityManagerBeanName) {
        try {
            if ("".equals(entityManagerBeanName)) {
                return entityManagerInstance.select(EntityManager.class, new DefaultAnnotation()).get();
            } else {
                return entityManagerInstance.select(EntityManager.class, new RiderPUAnnotation(entityManagerBeanName) {
                }).get();
            }
        } catch (Exception e) {
            log.warn("Could not resolve entity manager named {}. Default one will be used.", entityManagerBeanName, e);
            return entityManagerInstance.select(EntityManager.class, new DefaultAnnotation()).get();//quarkus wont have multiple entity manager (for now)
        }
    }

    /**
     * unfortunately there is no standard way to get jdbc connection from JPA entity manager
     *
     * @return JDBC connection
     */
    private Connection createConnection(String entityManagerBeanName) {
        try {
            if (isJta()) {
                return jtaConnectionHolder.get().getConnection(entityManagerBeanName);
            } else {
                if (isHibernatePresentOnClasspath() && entityManager.getDelegate() instanceof Session) {
                    connection = ((SessionImpl) entityManager.unwrap(Session.class)).connection();
                } else {
                    /**
                     * see here:http://wiki.eclipse.org/EclipseLink/Examples/JPA/EMAPI#Getting_a_JDBC_Connection_from_an_EntityManager
                     */
                    EntityTransaction tx = this.entityManager.getTransaction();
                    tx.begin();
                    connection = entityManager.unwrap(Connection.class);
                    tx.commit();
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create database connection", e);
        }

        return connection;
    }

    boolean isJta() {
        if (isJta == null) {
            try {
                entityManager.getTransaction();
                isJta = false;
            } catch (IllegalStateException iex) {
                isJta = true;
            }
        }
        return isJta;
    }

    public void process(DataSetConfig dataSetConfig, DBUnitConfig config) {
        dataSetExecutor.setDBUnitConfig(config);
        dataSetExecutor.createDataSet(dataSetConfig);
    }

    private boolean isHibernatePresentOnClasspath() {
        try {
            Class.forName("org.hibernate.Session");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void clearDatabase(DataSetConfig DataSetConfig) {
        try {
            dataSetExecutor.clearDatabase(DataSetConfig);
        } catch (SQLException e) {
            log.error("Could not clear database.", e);
        }
    }

    public void executeStatements(String[] executeStatementsAfter) {
        dataSetExecutor.executeStatements(executeStatementsAfter);
    }

    public void executeScript(String script) {
        dataSetExecutor.executeScript(script);
    }

    public void compareCurrentDataSetWith(DataSetConfig expected, String[] excludeCols, Class<? extends Replacer>[] replacers, String[] orderBy, CompareOperation compareOperation) throws DatabaseUnitException {
        dataSetExecutor.compareCurrentDataSetWith(expected, excludeCols, replacers, orderBy, compareOperation);
    }

    public Connection getConnection() {
        return connection;
    }

    public void exportDataSet(Method method) {
        ExportDataSet exportDataSet = resolveExportDataSet(method);
        if (exportDataSet != null) {
            DataSetExportConfig exportConfig = DataSetExportConfig.from(exportDataSet);
            String outputName = exportConfig.getOutputFileName();
            if (outputName == null || "".equals(outputName.trim())) {
                outputName = method.getName().toLowerCase() + "." + exportConfig.getDataSetFormat().name().toLowerCase();
            }
            exportConfig.outputFileName(outputName);
            try {
                DataSetExporter.getInstance().export(getConnection(), exportConfig);
            } catch (Exception e) {
                log.warn("Could not export dataset after method " + method.getName(), e);
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

    public void enableConstraints() {
        try {
            dataSetExecutor.enableConstraints();
        } catch (SQLException e) {
            log.warn("Could not enable constraints.", e);
        }
    }

    public DataSetExecutor getDataSetExecutor() {
        return dataSetExecutor;
    }

    public void afterTest(String datasource) {
        if (isJta()) {
            jtaConnectionHolder.get().tearDown(datasource);
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}