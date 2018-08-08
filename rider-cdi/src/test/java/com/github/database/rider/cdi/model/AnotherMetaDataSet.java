package com.github.database.rider.cdi.model;

import com.github.database.rider.core.api.dataset.DataSet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DataSet(value = "yml/expectedUser.yml", disableConstraints = true)
public @interface AnotherMetaDataSet {

}
