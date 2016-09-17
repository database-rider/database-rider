package com.github.database.rider.core.api.leak;

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

}
