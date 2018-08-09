package com.github.database.rider.core.replacers;

import org.dbunit.dataset.ReplacementDataSet;

/**
 * Common interface for all replacers.
 *
 * @author njuro
 */
public interface Replacer {


    /**
     * Registers new substitutions in data set through use of {@link ReplacementDataSet#addReplacementSubstring(String, String)}
     * and {@link ReplacementDataSet#addReplacementObject(Object, Object)}.
     *
     * @param dataSet - replacement set
     */
    void addReplacements(ReplacementDataSet dataSet);
}
