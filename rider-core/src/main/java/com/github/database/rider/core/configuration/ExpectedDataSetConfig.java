package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.replacers.Replacer;

public class ExpectedDataSetConfig {

    private String[] ignoreCols = {};
    private Class<? extends Replacer>[] replacers;
    private String[] orderBy = {};
    private CompareOperation compareOperation = CompareOperation.EQUALS;


    public ExpectedDataSetConfig ignoreCols(String... ignoredCols) {
        ignoreCols = ignoredCols;
        return this;
    }

    public ExpectedDataSetConfig replacers(Class<? extends Replacer>... replacers) {
        this.replacers = replacers;
        return this;
    }

    public ExpectedDataSetConfig orderBy(String... orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public ExpectedDataSetConfig compareOperation(CompareOperation compareOperation) {
        this.compareOperation = compareOperation;
        return this;
    }

    public String[] getIgnoreCols() {
        return ignoreCols;
    }

    public Class<? extends Replacer>[] getReplacers() {
        return replacers;
    }

    public String[] getOrderBy() {
        return orderBy;
    }

    public CompareOperation getCompareOperation() {
        return compareOperation;
    }
}
