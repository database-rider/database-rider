package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSetExecutor;
import com.github.database.rider.core.api.leak.LeakHunter;
import com.github.database.rider.core.configuration.DBUnitConfig;

public class DBUnitTestContext {

	private final DataSetExecutor executor;
	private final DBUnitConfig dbUnitConfig;
	private final LeakHunter leakHunter;

    public DBUnitTestContext(DataSetExecutor executor, DBUnitConfig dbUnitConfig, LeakHunter leakHunter) {
        this.executor = executor;
        this.dbUnitConfig = dbUnitConfig;
        this.leakHunter = leakHunter;
    }

    public DataSetExecutor getExecutor() {
		return executor;
	}

    public DBUnitConfig getDbUnitConfig() {
        return dbUnitConfig;
    }

    public LeakHunter getLeakHunter() {
        return leakHunter;
    }
}
