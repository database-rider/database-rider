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
   * @return list of dataset file names using 'resources' or 'resources/datasets' folder as root directory.
   * Single dataset with multiple comma separated dataset file names can also be provided.
   * Also URL-Notation is supported, e.g: 'file:///C:/dir/users.xml' OR 'http://...'
   */
  String[] value() default "";

  /**
   *
   * @return column names to ignore in comparison
   */
  String[] ignoreCols() default "";
  
  /**
   * @return implementations of {@link Replacer} called during reading expected dataset before comparison
   */
  Class<? extends Replacer>[] replacers() default {};

  /**
   * @return column names to sort the dataset with
   */
  String[] orderBy() default {};

  CompareOperation compareOperation() default CompareOperation.EQUALS;
  
  /**
   * @return a dataset provider implementation responsible for generating the expected dataset programatically instead of providing an external file defining the dataset.
   */
  Class<? extends DataSetProvider> provider() default DataSetProvider.class;
}