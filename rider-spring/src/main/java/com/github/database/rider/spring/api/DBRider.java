package com.github.database.rider.spring.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.database.rider.core.connection.RiderDataSource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestExecutionListeners;

import com.github.database.rider.spring.DBRiderTestExecutionListener;

/**
 * Shortcut to enable database rider tests.
 * Replaces @TestExecutionListeners(value = DBRiderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS).
 * @see com.github.database.rider.spring.DBRiderTestExecutionListener
 *
 * @author Artemy Osipov
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@TestExecutionListeners(value = DBRiderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface DBRider {

  /**
   * @return name of the DataSource bean in Spring Context.
   * If empty then dataSource bean will be loaded by class and thus default one will be used.
   */
  @AliasFor("value")
  String dataSourceBeanName() default "";

  /**
   * @return the expected database type.
   * If empty then do not validate database type.
   * @throws IllegalArgumentException If the expected database type different from that of context.
   */
  RiderDataSource.DBType dataBaseType() default RiderDataSource.DBType.UNKNOWN;
}
