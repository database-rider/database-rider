package com.github.database.rider.core.api.configuration;

/**
 * Enumeration denoting the used orthography ('letter-case') for database identifiers (tables, columns), if and only if
 * database-rider is configured in case-insensitive mode (i.e. "<code>caseSensitiveTableNames=false</code>").
 * 
 * @since 1.1.1
 */
public enum Orthography {

    /**
     * Orthography 'upper-case'. Words (identifiers) are changed having upper-case letters only.
     */
    UPPERCASE,

    /**
     * Orthography 'lower-case'. Words (identifiers) are changed having lower-case letters only.
     */
    LOWERCASE;
}
