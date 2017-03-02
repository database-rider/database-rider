# order: 3
Feature: Configuration
====
[quote]
____
In order to handle various use cases
As a developer
I want to be able to configure DataBase Rider
____
====

Scenario: DataSet configuration

DataSet configuration is done via *@DataSet* annotation at *class* or *method* level:


[source,java]
----
  \@Test
  \@DataSet(value ="users.yml", strategy = SeedStrategy.UPDATE,
    disableConstraints = true,cleanAfter = true,
    useSequenceFiltering = true, tableOrdering = {"TWEET","USER"},
    executeScriptsBefore = "script.sql", executeStatementsBefore = "DELETE from USER where 1=1"
    transactional = true, cleanAfter=true)
  public void shouldCreateDataSet(){

  }
----


Table below illustrate the possible configurations:


  [cols="3*", options="header"]
  |===
  |Name | Description | Default
  |value| Dataset file name using test resources folder as root directory. Multiple, comma separated, dataset file names can be provided.| ""
  |executorId| Name of dataset executor for the given dataset.| DataSetExecutorImpl.DEFAULT_EXECUTOR_ID
  |strategy| DataSet seed strategy. Possible values are: CLEAN_INSERT, INSERT, REFRESH and UPDATE.| CLEAN_INSERT, meaning that DBUnit will clean and then insert data in tables present in provided dataset.
  |useSequenceFiltering| If true dbunit will look at constraints and dataset to try to determine the correct ordering for the SQL statements.| true
  |tableOrdering| A list of table names used to reorder DELETE operations to prevent failures due to circular dependencies.| ""
  |disableConstraints| Disable database constraints.| false
  |cleanBefore| If true Database Rider will try to delete database before test in a smart way by using table ordering and brute force.| false
  |cleanAfter| If true Database Rider will try to delete database after test in a smart way by using table ordering and brute force.| false
  |transactional| If true a transaction will be started before and committed after test execution. | false
  |executeStatementsBefore| A list of jdbc statements to execute before test.| {}
  |executeStatementsAfter| A list of jdbc statements to execute after test.| {}
  |executeScriptsBefore| A list of sql script files to execute before test. Note that commands inside sql file must be separated by `;`.| {}
  |executeScriptsAfter| A list of sql script files to execute after test. Note that commands inside sql file must be separated by `;`.| {}
  |===

Scenario: DBUnit configuration

  `DBUnit`, the tool doing the dirty work the scenes, can be configured by *@DBUnit* annotation (class or method level) and *dbunit.yml* file present in `test resources` folder.

[source,java]
----
\@Test
\@DBUnit(cacheConnection = true, cacheTableNames = false, allowEmptyFields = true,batchSize = 50)
public void shouldLoadDBUnitConfigViaAnnotation() {

}
----

Here is a dbunit.yml example, also the default values:

.src/test/resources/dbunit.yml
----
cacheConnection: true <1>
cacheTableNames: true <2>
leakHunter: false <3>
  properties:
    batchedStatements:  false <4>
    qualifiedTableNames: false <5>
    caseSensitiveTableNames: false <6>
    batchSize: 100 <7>
    fetchSize: 100 <8>
    allowEmptyFields: false <9>
    escapePattern: <10>
  connectionConfig: <11>
    driver: ""
    url: ""
    user: ""
    password: ""
----
<1> Database connection will be reused among tests
<2> Caches table names to avoid query connection metadata unnecessarily
<3> Activate connection leak detection. In case a leak (open JDBC connections is increased after test execution) is found an exception is thrown and test fails.
<4> Enables usage of JDBC batched statement
<5> Enable or disable multiple schemas support. If enabled, Dbunit access tables with names fully qualified by schema using this format: SCHEMA.TABLE.
<6> Enable or disable case sensitive table names. If enabled, Dbunit handles all table names in a case sensitive way.
<7> Specifies the size of JDBC batch updates
<8> Specifies statement fetch size for loading data into a result set table.
<9> Allow to call INSERT/UPDATE with empty strings ('').
<10> Allows schema, table and column names escaping. The property value is an escape pattern where the ? is replaced by the name. For example, the pattern "[?]" is expanded as "[MY_TABLE]" for a table named "MY_TABLE". The most common escape pattern is "\"?\"" which surrounds the table name with quotes (for the above example it would result in "\"MY_TABLE\""). As a fallback if no questionmark is in the given String and its length is one it is used to surround the table name on the left and right side. For example the escape pattern "\"" will have the same effect as the escape pattern "\"?\"".
<11> JDBC connection configuration, it will be used in case you don't provide a connection inside test (except in CDI test where connection is inferred from entity manager).

NOTE: `@DBUnit` annotation takes precedence over `dbunit.yml` global configuration which will be used only if the annotation is not present.

[TIP]
=====
Since version 1.1.0 you can define only the properties of your interest, example:

----
cacheConnection: false
  properties:
    caseSensitiveTableNames: true
    escapePattern: "\"?\""
----
=====
