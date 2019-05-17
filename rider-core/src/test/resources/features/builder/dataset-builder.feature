# order: 10
Feature: DataSet builder
====
[quote]
____
In order to create datasets programmatically
As a developer
I want to use `DatasetBuilder` API.
____
====

TIP: Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/DataSetProviderIt.java#L36[found here^].

  Scenario: Create dataset using dataset builder


    #cukedoctor-discrete
    Given The following method declaration
    """
[source,java]
----
include::../../../src/test/java/com/github/database/rider/core/DataSetProviderIt.java[tags=signature]
----
<1> `provider` attribute expects a class which implements DataSetProvider interface.

    """

#cukedoctor-discrete
#{TIP: For more complex examples of programmatic dataset creation, https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/dataset/builder/DatasetBuilderTest.java#L27[see here^].}
    And The following dataset provider implementation

 """
[source,java]
----
include::../../../src/test/java/com/github/database/rider/core/DataSetProviderIt.java[tags=provider]
----
<1> Starts a table on current dataset
<2> Starts creating a row for current table
<3> Adds a column witn name `id` and value `1` to current row
<4> Starts creating another row for table `user`
<5> creates the dataset.

 """

#cukedoctor-discrete
    When The test is executed

    #cukedoctor-discrete
#{NOTE: yaml format is used only for illustration here, when using DatasetBuilder the dataset is only created in memory, it is not materialized in any file (unless it is <<DataSet-export, exported>>). }
    Then The following dataset will be used for seeding the database

 """
 ----
USER:
  - ID: 1
    NAME: "@dbunit"
  - ID: 2
    NAME: "@dbrider"
 ----

 """


  Scenario: Create dataset using dataset builder with `column...values` syntax

    `DataSetBuilder` has an alternative syntax, similar to SQL `insert into values`, which may be more appropriated to datasets with *few columns* and *lot of rows*.

    #cukedoctor-discrete
    Given The following method declaration
    """
[source,java]
----
include::../../../src/test/java/com/github/database/rider/core/DataSetProviderIt.java[tags=signature2]
----
    """

#cukedoctor-discrete
#{NOTE: The columns are specified only one time and the values are 'index' based (first value refers to first column. }
    And The following dataset provider implementation

 """
[source,java]
----
include::../../../src/test/java/com/github/database/rider/core/DataSetProviderIt.java[tags=provider2]
----
<1> Starts a table on current dataset
<2> Declares columns involved in current dataset
<3> specify values for each column

 """

#cukedoctor-discrete
    When The test is executed

    #cukedoctor-discrete
#{NOTE: yaml format is used only for illustration here, when using DatasetBuilder the dataset is only created in memory, it is not materialized in any file (unless it is <<DataSet-export, exported>>). }
    Then The following dataset will be used for seeding the database

 """
 ----
USER:
  - ID: 1
    NAME: "@dbunit"
  - ID: 2
    NAME: "@dbrider"
 ----

 """