package com.github.database.rider.core.replacers;

import org.dbunit.dataset.ReplacementDataSet;

/**
 * Replacer which replaces [null] placehoder with actual {@code null} value.
 *
 * @author njuro
 */
public class NullReplacer implements Replacer {

    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        dataSet.addReplacementObject("[null]", null);
    }
}
