package com.github.database.rider.junit5;

import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(DBUnitExtension.class)
@DataSet(value = "users.yml", disableConstraints = true)
public @interface MetaDataSet {


}
