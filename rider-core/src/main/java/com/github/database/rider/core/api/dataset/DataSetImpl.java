/*
 * Copyright 2018 rmpestano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.database.rider.core.api.dataset;

import java.lang.annotation.Annotation;

/**
 *
 * @author rmpestano
 */
public class DataSetImpl implements DataSet {
    
    private static DataSetImpl instance;

    private String[] value;
    private String executorId;
    private SeedStrategy strategy;
    private boolean useSequenceFiltering;
    private String[] tableOrdering;
    private boolean disableConstraints;
    private boolean fillIdentityColumns;
    private String[] executeStatementsBefore;
    private String[] executeScriptsAfter;
    private String[] executeScriptsBefore;
    private String[] executeStatementsAfter;
    private boolean cleanBefore;
    private boolean cleanAfter;
    private boolean transactional;

    public DataSetImpl() {
    }
    
    public DataSetImpl(String[] value, String executorId, SeedStrategy strategy, boolean useSequenceFiltering, String[] tableOrdering, boolean disableConstraints, boolean fillIdentityColumns, String[] executeStatementsBefore, String[] executeScriptsAfter, String[] executeScriptsBefore, String[] executeStatementsAfter, boolean cleanBefore, boolean cleanAfter, boolean transactional) {
        this.value = value;
        this.executorId = executorId;
        this.strategy = strategy;
        this.useSequenceFiltering = useSequenceFiltering;
        this.tableOrdering = tableOrdering;
        this.disableConstraints = disableConstraints;
        this.fillIdentityColumns = fillIdentityColumns;
        this.executeStatementsBefore = executeStatementsBefore;
        this.executeScriptsAfter = executeScriptsAfter;
        this.executeScriptsBefore = executeScriptsBefore;
        this.executeStatementsAfter = executeStatementsAfter;
        this.cleanBefore = cleanBefore;
        this.cleanAfter = cleanAfter;
        this.transactional = transactional;
    }
    
    public static DataSetImpl instance() {
        instance = new DataSetImpl();
        return instance;
    }
    
    public DataSetImpl withValue(String... value) {
        instance.value = value;
        return instance;
    } 
    
    public DataSetImpl withExecuteScriptsBefore(String... executeScriptsBefore) {
        instance.executeScriptsBefore = executeScriptsBefore;
        return instance;
    } 
    
    public DataSetImpl withExecuteScriptsAfter(String... executeScriptsAfter) {
        instance.executeScriptsAfter = executeScriptsAfter;
        return instance;
    } 
    
     public DataSetImpl withExecuteStatementsBefore(String... executeStatementsBefore) {
        instance.executeStatementsBefore = executeStatementsBefore;
        return instance;
    } 
    
    public DataSetImpl withExecuteStatementsAfter(String... executeStatementsAfter) {
        instance.executeStatementsAfter = executeStatementsAfter;
        return instance;
    } 
    
     public DataSetImpl withTableOrdering(String... tableOrdering) {
        instance.tableOrdering = tableOrdering;
        return instance;
    } 
    

    @Override
    public String[] value() {
        return this.value;
    }

    @Override
    public String executorId() {
        return this.executorId;
    }

    @Override
    public SeedStrategy strategy() {
        return this.strategy;
    }

    @Override
    public boolean useSequenceFiltering() {
        return this.useSequenceFiltering;
    }

    @Override
    public String[] tableOrdering() {
        return this.tableOrdering;
    }

    @Override
    public boolean disableConstraints() {
        return this.disableConstraints;
    }

    @Override
    public boolean fillIdentityColumns() {
        return this.fillIdentityColumns;
    }

    @Override
    public String[] executeStatementsBefore() {
        return this.executeStatementsBefore;
    }

    @Override
    public String[] executeStatementsAfter() {
        return this.executeStatementsAfter;
    }

    @Override
    public String[] executeScriptsBefore() {
        return this.executeScriptsBefore;
    }

    @Override
    public String[] executeScriptsAfter() {
        return this.executeScriptsAfter;
    }

    @Override
    public boolean cleanBefore() {
        return this.cleanBefore;
    }

    @Override
    public boolean cleanAfter() {
        return this.cleanAfter;
    }

    @Override
    public boolean transactional() {
        return this.transactional;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DataSet.class;
    }
    
}
