package com.github.database.rider.configuration;

import com.github.database.rider.api.dataset.DataSet;
import com.github.database.rider.api.dataset.SeedStrategy;
import com.github.database.rider.dataset.DataSetExecutorImpl;

/**
 * Created by pestano on 26/07/15.
 */
public class DataSetConfig {

    private String name;
    private String executorId = DataSetExecutorImpl.DEFAULT_EXECUTOR_ID;
    private SeedStrategy strategy = SeedStrategy.CLEAN_INSERT;
    private boolean useSequenceFiltering = true;
    private boolean disableConstraints = false;
    private boolean cleanBefore = false;
    private boolean cleanAfter = false;
    private boolean transactional = false;
    private String[] tableOrdering = {};
    private String[] executeStatementsBefore = {};
    private String[] executeStatementsAfter = {};
    private String[] executeScriptsBefore = {};
    private String[] executeScriptsAfter = {};


    public DataSetConfig() {
        //loadDefaultConfiguration();
    }

    public DataSetConfig(String name) {
        this.name = name;
    }

    public DataSetConfig name(String name) {
        this.name = name;
        return this;
    }

    public DataSetConfig strategy(SeedStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public DataSetConfig useSequenceFiltering(boolean useSequenceFiltering) {
        this.useSequenceFiltering = useSequenceFiltering;
        return this;
    }

    public DataSetConfig disableConstraints(boolean disableConstraints) {
        this.disableConstraints = disableConstraints;
        return this;
    }

    public DataSetConfig tableOrdering(String[] tableOrdering) {
        this.tableOrdering = tableOrdering;
        return this;
    }

    public DataSetConfig cleanBefore(boolean cleanBefore) {
        this.cleanBefore = cleanBefore;
        return this;
    }

    public DataSetConfig cleanAfter(boolean cleanAfter) {
        this.cleanAfter = cleanAfter;
        return this;
    }

    public DataSetConfig executeStatementsBefore(String[] executeStatementsBefore) {
        this.executeStatementsBefore = executeStatementsBefore;
        return this;
    }

    public DataSetConfig executeStatementsAfter(String[] executeStatementsAfter) {
        this.executeStatementsAfter = executeStatementsAfter;
        return this;
    }

    public DataSetConfig executeScripsBefore(String[] executeScriptsBefore) {
        this.executeScriptsBefore = executeScriptsBefore;
        return this;
    }

    public DataSetConfig executeScriptsAfter(String[] executeScriptsAfter) {
        this.executeScriptsAfter = executeScriptsAfter;
        return this;
    }

    /**
     *
     * @param executorId name of dataset executor for the given dataset. If not specified the default one will be used.
     *
     * Use this option to work with multple database conncetions. Remember that each executor has its own connection.
     * @return DataSetConfig with executor name configured
     */
    public DataSetConfig executorId(String executorId) {
        this.executorId = executorId;
        return this;
    }

    public DataSetConfig transactional(boolean transactional){
        this.transactional = transactional;
        return this;
    }


    public DataSetConfig from(DataSet dataSet) {
        if(dataSet != null){
            return name(dataSet.value()).strategy(dataSet.strategy()).
                    useSequenceFiltering(dataSet.useSequenceFiltering()).
                    tableOrdering(dataSet.tableOrdering()).
                    disableConstraints(dataSet.disableConstraints()).
                    executorId(dataSet.executorId()).
                    executeStatementsBefore(dataSet.executeStatementsBefore()).
                    executeScripsBefore(dataSet.executeScriptsBefore()).
                    cleanBefore(dataSet.cleanBefore()).
                    cleanAfter(dataSet.cleanAfter()).
                    transactional(dataSet.transactional()).
                    executeStatementsAfter(dataSet.executeStatementsAfter()).
                    executeScriptsAfter(dataSet.executeScriptsAfter());
        } else{
            throw new RuntimeException("Cannot create DataSetConfig from Null DataSet");
        }

    }


    public String getName() {
        return name;
    }

    public SeedStrategy getstrategy() {
        return strategy;
    }

    public boolean isUseSequenceFiltering() {
        return useSequenceFiltering;
    }

    public boolean isDisableConstraints() {
        return disableConstraints;
    }

    public boolean isTransactional() {
       return transactional;
    }

    public String[] getTableOrdering() {
        return tableOrdering;
    }

    public String[] getExecuteStatementsBefore() {
        return executeStatementsBefore;
    }

    public String[] getExecuteStatementsAfter() {
        return executeStatementsAfter;
    }

    public String[] getExecuteScriptsBefore() {
        return executeScriptsBefore;
    }

    public String[] getExecuteScriptsAfter() {
        return executeScriptsAfter;
    }

    public String getExecutorId() {
        return executorId;
    }

    public boolean isCleanBefore() {
        return cleanBefore;
    }

    public boolean isCleanAfter() {
        return cleanAfter;
    }

    public void setstrategy(SeedStrategy strategy) {
        this.strategy = strategy;
    }

    public void setUseSequenceFiltering(boolean useSequenceFiltering) {
        this.useSequenceFiltering = useSequenceFiltering;
    }

    public void setDisableConstraints(boolean disableConstraints) {
        this.disableConstraints = disableConstraints;
    }

    public void setCleanBefore(boolean cleanBefore) {
        this.cleanBefore = cleanBefore;
    }

    public void setCleanAfter(boolean cleanAfter) {
        this.cleanAfter = cleanAfter;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    @Override
    public String toString() {
        return name;
    }
}
