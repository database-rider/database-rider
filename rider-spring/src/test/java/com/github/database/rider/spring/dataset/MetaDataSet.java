package com.github.database.rider.spring.dataset;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DBUnit(caseSensitiveTableNames = true)
@DBRider
@DataSet
public @interface MetaDataSet {

    @AliasFor(annotation = DataSet.class, attribute = "value")
    String[] value() default "test.yml";

}
