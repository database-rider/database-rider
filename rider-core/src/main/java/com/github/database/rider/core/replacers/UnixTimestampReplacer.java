package com.github.database.rider.core.replacers;

import org.dbunit.dataset.ReplacementDataSet;

/**
 * Replacer which replaces [UNIX_TIMESTAMP] placeholder with Unix timestamp (obtained through {@link System#currentTimeMillis()}
 *
 * @author njuro
 */
public class UnixTimestampReplacer implements Replacer {

    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        long timestamp = System.currentTimeMillis() / 1000L;
        dataSet.addReplacementObject("[UNIX_TIMESTAMP]", timestamp);
    }
}
