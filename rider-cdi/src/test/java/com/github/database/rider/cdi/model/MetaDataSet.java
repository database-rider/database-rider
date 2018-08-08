package com.github.database.rider.cdi.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.database.rider.core.api.dataset.DataSet;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DataSet(value = "yml/users.yml", disableConstraints = true)
public @interface MetaDataSet {


}
