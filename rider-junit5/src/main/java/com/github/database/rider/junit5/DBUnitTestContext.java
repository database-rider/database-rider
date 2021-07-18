package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;

public class DBUnitTestContext {

	private DataSetExecutor executor;
	private LeakHunter leakHunter;

    public DBUnitTestContext(DataSetExecutor executor, LeakHunter leakHunter) {
        this.executor = executor;
        this.leakHunter = leakHunter;
    }

    public DataSetExecutor getExecutor() {
		return executor;
	}

	public LeakHunter getLeakHunter() {
		return leakHunter;
	}

}
