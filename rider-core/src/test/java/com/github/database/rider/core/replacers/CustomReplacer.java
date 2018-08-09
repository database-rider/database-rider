package com.github.database.rider.core.replacers;

import org.dbunit.dataset.ReplacementDataSet;

import java.util.Objects;

/**
 * Example implementation of {@link Replacer} which replaces string 'FOO' for 'BAR'
 *
 * @author njuro
 */
public class CustomReplacer implements Replacer {

    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        dataSet.addReplacementSubstring("FOO", "BAR");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}
