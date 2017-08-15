package com.github.database.rider.core.api.configuration;

/**
 * Enumeration denoting the used 'letter-case' for database identifiers (tables, columns), if database-rider is
 * configured in case-insensitive mode (i.e. "<code>caseSensitiveTableNames=false</code>").
 * 
 * @since 1.1.1
 */
public enum LetterCase {

    /**
     * Upper-case
     */
    UPPER,

    /**
     * Lower-case.
     */
    LOWER;
}
