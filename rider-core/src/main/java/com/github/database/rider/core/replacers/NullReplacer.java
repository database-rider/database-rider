package com.github.database.rider.core.replacers;

import org.dbunit.dataset.ReplacementDataSet;

/**
 * Replacer which replaces [null] placehoder with actual {@code null} value.
 *
 * @author njuro
 */
public class NullReplacer implements Replacer {

    public static final String NULL = "[null]";

    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        dataSet.addReplacementObject(NULL, null);
    }
}
