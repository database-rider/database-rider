package com.github.database.rider.core.api.configuration;

public enum DataSetMergingStrategy {

    /**
     * When priority is of merging is set to CLASS then first load class level datasets when merging
     */
    CLASS,
    /**
     * When priority is of merging is set to METHOD then first load method level datasets when merging
     */
    METHOD

}
