package com.github.database.rider.cdi;

import java.io.Serializable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

import com.github.database.rider.cdi.api.DBUnitInterceptor;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.leak.LeakHunterFactory;
import com.github.database.rider.core.util.AnnotationUtils;

/**
 * Created by pestano on 26/07/15.
 */
@Interceptor
@DBUnitInterceptor
public class DBUnitInterceptorImpl implements Serializable {

    @Inject
    DataSetProcessor dataSetProcessor;

    @Inject
    private EntityManager em;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext)
            throws Exception {

        Object proceed = null;
        DataSet usingDataSet = resolveDataSet(invocationContext);

        if (usingDataSet != null) {
            DataSetConfig dataSetConfig = new DataSetConfig(usingDataSet.value()).
                    cleanAfter(usingDataSet.cleanAfter()).
                    cleanBefore(usingDataSet.cleanBefore()).
                    disableConstraints(usingDataSet.disableConstraints()).
                    executeScripsBefore(usingDataSet.executeScriptsBefore()).
                    executeScriptsAfter(usingDataSet.executeScriptsAfter()).
                    executeStatementsAfter(usingDataSet.executeStatementsAfter()).
                    executeStatementsBefore(usingDataSet.executeStatementsBefore()).
                    strategy(usingDataSet.strategy()).
                    transactional(usingDataSet.transactional()).
                    tableOrdering(usingDataSet.tableOrdering()).
                    useSequenceFiltering(usingDataSet.useSequenceFiltering());
            DBUnitConfig dbUnitConfig = DBUnitConfig.from(invocationContext.getMethod());
            dataSetProcessor.process(dataSetConfig,dbUnitConfig);
            boolean isTransactionalTest = dataSetConfig.isTransactional();
            if(isTransactionalTest){
                em.getTransaction().begin();
            }
            LeakHunter leakHunter = null;
            try {
                if (dbUnitConfig.isLeakHunter()) {
                    leakHunter = LeakHunterFactory.from(dataSetProcessor.getDataSetExecutor().getRiderDataSource(), invocationContext.getMethod().getName());
                    leakHunter.measureConnectionsBeforeExecution();
                }
                proceed = invocationContext.proceed();

                if(isTransactionalTest){
                    em.getTransaction().commit();
                }
                ExpectedDataSet expectedDataSet = invocationContext.getMethod().getAnnotation(ExpectedDataSet.class);
                if(expectedDataSet != null){
                    dataSetProcessor.compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value()).disableConstraints(true),expectedDataSet.ignoreCols());
                }
            } finally {
                if(isTransactionalTest && em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }

                if(leakHunter != null){
                    leakHunter.checkConnectionsAfterExecution();
                }

                dataSetProcessor.exportDataSet(invocationContext.getMethod());

                if (!"".equals(usingDataSet.executeStatementsAfter())) {
                    dataSetProcessor.executeStatements(dataSetConfig.getExecuteStatementsAfter());
                }

                if(usingDataSet.executeScriptsAfter().length > 0 && !"".equals(usingDataSet.executeScriptsAfter()[0])){
                    for (int i = 0; i < usingDataSet.executeScriptsAfter().length; i++) {
                        dataSetProcessor.executeScript(usingDataSet.executeScriptsAfter()[i]);
                    }
                }

                if(usingDataSet.cleanAfter()){
                    dataSetProcessor.clearDatabase(dataSetConfig);
                }

                dataSetProcessor.enableConstraints();
                em.clear();
            }//end finally



        } else{//no dataset provided, just proceed and check expectedDataSet
            try {
                proceed = invocationContext.proceed();
                ExpectedDataSet expectedDataSet = invocationContext.getMethod().getAnnotation(ExpectedDataSet.class);
                if(expectedDataSet != null){
                    dataSetProcessor.compareCurrentDataSetWith(new DataSetConfig(expectedDataSet.value()).disableConstraints(true),expectedDataSet.ignoreCols());
                }
            }finally {
                dataSetProcessor.exportDataSet(invocationContext.getMethod());
            }

        }


        return proceed;
    }

    private DataSet resolveDataSet(InvocationContext invocationContext) {
        DataSet usingDataSet = AnnotationUtils.findAnnotation(invocationContext.getMethod(), DataSet.class);
        if (usingDataSet == null) {
            usingDataSet = AnnotationUtils.findAnnotation(invocationContext.getMethod().getDeclaringClass(),DataSet.class);
        }

        return usingDataSet;

    }


}
