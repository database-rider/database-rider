package com.github.database.rider.core.api.leak;

import com.github.database.rider.core.leak.LeakHunterException;

/**
 * Created by pestano on 07/09/16.
 *
 * based on https://vladmihalcea.com/2016/07/12/the-best-way-to-detect-database-connection-leaks/
 */
public interface LeakHunter {

    /**
     *
     * @return number of opened jdbc connections/sessions
     */
    int openConnections();

    /**
     * @return number of opened jdbc connections/sessions
     */
    int measureConnectionsBeforeExecution();

    /**
     * Check number of opened jdbc connections/sessions
     * @throws LeakHunterException if the number of connections is greater than before execution
     */
    void checkConnectionsAfterExecution() throws LeakHunterException;
}
