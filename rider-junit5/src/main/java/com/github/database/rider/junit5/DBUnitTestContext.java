package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;

public class DBUnitTestContext {

    private final DataSetExecutor executor;
    private LeakHunter leakHunter;

    public DBUnitTestContext(DataSetExecutor executor) {
        this.executor = executor;
    }

    public DataSetExecutor getExecutor() {
        return executor;
    }

    public void setLeakHunter(LeakHunter leakHunter) {
        this.leakHunter = leakHunter;
    }

    public LeakHunter getLeakHunter() {
        return leakHunter;
    }
}
