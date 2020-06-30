package com.github.database.rider.core.api.dataset;

import com.github.database.rider.core.replacers.Replacer;

import java.lang.annotation.*;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSet {

  /**
   * @return list of dataset file names using 'resources' or 'resouces/datasets' folder as root directory.
   * Single dataset with multiple comma separated dataset file names can also be provided.
   * Also URL-Notation is supported, e.g: 'file:///C:/dir/users.xml' OR 'http://...'
   */
  String[] value() default "";

  /**
   *
   * @return name of dataset executor for the given dataset. If not specified the default one will be used.
   *
   * Use this option to work with multiple database connections. Remember that each executor has its own connection.
   */
  String executorId() default "";

  /**
   * @return DataSet seed strategy. Default is CLEAN_INSERT, meaning that DBUnit will clean and then insert data in tables present in provided dataset.
   */
  SeedStrategy strategy() default SeedStrategy.CLEAN_INSERT;

  /**
   * @return if true dbunit will look at constraints and dataset to try to determine the correct ordering for the SQL statements
   */
  boolean useSequenceFiltering() default true;

  /**
   * @return a list of table names used to reorder DELETE operations to prevent failures due to circular dependencies
   *
   */
  String[] tableOrdering() default {};


  boolean disableConstraints() default false;

  /**
   * @return true if dataset contains values for identity columns (some databases like MS SQL Server have issues with such cases)
   */
  boolean fillIdentityColumns() default false;

  /**
   * @return a list of jdbc statements to execute before test
   *
   */
  String[] executeStatementsBefore() default {};

  /**
   * @return a list of jdbc statements to execute after test
   */
  String[] executeStatementsAfter() default {};

  /**
   * @return a list of sql script files to execute before test.
   * Note that commands inside sql file must be separated by ';'
   *
   */
  String[] executeScriptsBefore() default {};

  /**
   * @return a list of sql script files to execute after test.
   * Note that commands inside sql file must be separated by ';'
   */
  String[] executeScriptsAfter() default {};

  /**
   * @return if true Database Rider will try to delete database before test in a 'smart way' by using table ordering and brute force.
   */
  boolean cleanBefore() default false;

  /**
   * @return if true Database Rider will try to delete database after test in a 'smart way'
   */
  boolean cleanAfter() default false;

  /**
   *
   * @return if true a transaction will be started before test and committed after test execution. Note that it will only work for JPA based tests, in other words, EntityManagerProvider.isEntityManagerActive() must be true.
   *
   */
  boolean transactional() default false;


  /**
   * @return a dataset provider implementation responsible for generating the dataset programatically instead of providing an external file defining the dataset.
   */
  Class<? extends DataSetProvider> provider() default DataSetProvider.class;

    /**
     * By default ALL tables are cleaned when <code>cleanBefore</code> or <code>cleanAfter</code> is set to <code>true</code>.
     *
     * Allows user to provide tables which will NOT be cleaned in <code>cleanBefore</code> and <code>cleanAfter</code>.
     *
     * @return list of table names to skip the cleaning in <code>cleanBefore</code> and/or <code>cleanAfter</code>. If empty all tables will be cleaned when cleanBefore() or cleanAfter() is set to <code>true</code>
     */
  String[] skipCleaningFor() default {};

    /**
     * @return implementations of {@link Replacer} to be used as dataset replacement during seeding database.
     * Note that DataSet level replacer will <b>override</b> global level replacers.
     */
    Class<? extends Replacer>[] replacers() default {};
}