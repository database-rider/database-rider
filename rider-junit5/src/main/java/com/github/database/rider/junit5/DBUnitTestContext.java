package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;

public class DBUnitTestContext {

	private DataSetExecutor executor;
	private LeakHunter leakHunter;

	public DataSetExecutor getExecutor() {
		return executor;
	}

	public DBUnitTestContext setExecutor(DataSetExecutor executor) {
		this.executor = executor;
		return this;
	}

	public LeakHunter getLeakHunter() {
		return leakHunter;
	}

	public DBUnitTestContext setLeakHunter(LeakHunter leakHunter) {
		this.leakHunter = leakHunter;
		return this;
	}
}
