package com.github.database.rider.core.api.expoter;

import com.github.database.rider.core.api.dataset.DataSetFormat;

import java.lang.annotation.*;

/**
 * Created by rafael-pestano on 30/08/2016.
 * 
 * This annotation configures DBUnit properties
 * (http://dbunit.sourceforge.net/properties.html) for a given dataset executor.
 * 
 * It can be used at class or method level.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExportDataSet {

    /**
     *
     * @return Output format of generated dataset.
     */
    DataSetFormat format() default DataSetFormat.YML;

    /**
     * @return tables to inlude in exported dataset. If empty all tables will be exported
     */
    String[] includeTables() default {};

    /**
     * @return list of select statements which the result will be present in exported dataset.
     */
    String[] queryList() default {};

    /**
     *
     * @return if true will bring dependent tables of declared includeTables.
     */
    boolean dependentTables() default false;

    String outputName() default "";
}