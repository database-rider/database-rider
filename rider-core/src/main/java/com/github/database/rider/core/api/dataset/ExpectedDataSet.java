package com.github.database.rider.core.api.dataset;

import java.lang.annotation.*;

import com.github.database.rider.core.replacers.Replacer;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExpectedDataSet {

  /**
   * @return dataset file name using resources folder as root directory
   */
  String[] value();

  /**
   *
   * @return column names to ignore in comparison
   */
  String[] ignoreCols() default "";
  
  /**
   * @return implementations of {@link Replacer} called during reading expected dataset before comparison
   */
  Class<? extends Replacer>[] replacers() default {};
}