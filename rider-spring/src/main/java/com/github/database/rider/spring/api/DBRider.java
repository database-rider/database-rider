package com.github.database.rider.spring.api;

import com.github.database.rider.spring.DBRiderTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shortcut to enable database rider tests.
 * Replaces @TestExecutionListeners(value = DBRiderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS).
 * @see com.github.database.rider.spring.DBRiderTestExecutionListener
 *
 * @author Artemy Osipov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestExecutionListeners(value = DBRiderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface DBRider {
}
