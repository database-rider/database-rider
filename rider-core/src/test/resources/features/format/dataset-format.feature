# order: 2
Feature: DataSet creation
====
[quote]
____
In order to create datasets to feed tables
As a developer
I want to declare database state in external files.
____
====

NOTE: It is a good practice to move database preparation or any infrastructure code outside test logic, it increases test maintainability.

  Scenario: Creating a `YAML` dataset

    *YAML* stands for `yet another markup language` and is a very simple, lightweight yet powerful format.

    IMPORTANT: YAML is based on spaces indentation so be careful because any missing or additional space can lead to an incorrect dataset.

  TIP: Source code of the examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java/[found here^].

#cukedoctor-discrete
    Given The following dataset

 """
.src/test/resources/dataset/yml/users.yml
----
include::../../../src/test/resources/datasets/yml/users.yml[]
----
 """

#cukedoctor-discrete
    When The following test is executed:
 """
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java[tags=yml]
----
 """
    Then The database should be seeded with the dataset content before test execution


  Scenario: Creating a `JSON` dataset


  #cukedoctor-discrete
    Given The following dataset

 """
.src/test/resources/dataset/json/users.json
----
include::../../../src/test/resources/datasets/json/users.json[]
----
 """

  #cukedoctor-discrete
    When The following test is executed:
 """
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java[tags=json]
----
 """
    Then The database should be seeded with the dataset content before test execution


  Scenario: Creating a `XML` dataset


    #cukedoctor-discrete
    Given The following dataset

 """
.src/test/resources/dataset/xml/users.xml
----
include::../../../src/test/resources/datasets/xml/users.xml[]
----
 """

    #cukedoctor-discrete
    When The following test is executed:
 """
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java[tags=xml]
----
 """
    Then The database should be seeded with the dataset content before test execution

  Scenario: Creating a `XLS` dataset


 #cukedoctor-discrete
    Given The following dataset

 """
.src/test/resources/dataset/xls/users.xls
----
ID	NAME
1	@realpestano
2	@dbunit
----

NOTE: Each Excell `sheet` name is the *table name*, first row is *columns names* and remaining rows/cells are values.
 """

 #cukedoctor-discrete
    When The following test is executed:
 """
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java[tags=xls]
----
 """
    Then The database should be seeded with the dataset content before test execution

  Scenario: Creating a `CSV` dataset


#cukedoctor-discrete
    Given The following dataset

 """
.src/test/resources/dataset/csv/USER.csv
----
include::../../../src/test/resources/datasets/csv/USER.csv[]
----

.src/test/resources/dataset/csv/TWEET.csv
----
include::../../../src/test/resources/datasets/csv/TWEET.csv[]
----

NOTE: File name is *table name* and first row is *column names*.

.src/test/resources/dataset/csv/table-ordering.txt
----
include::../../../src/test/resources/datasets/csv/table-ordering.txt[]
----

IMPORTANT: CSV datasets are composed by multiple files (one per table) and a table ordering descriptor declaring the order of creation.
Also note that each csv dataset must be declared in its own folder because DBUnit will read all csv files present in dataset folder.
 """


#cukedoctor-discrete
    When The following test is executed:
 """
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/format/DataSetFormatIt.java[tags=csv]
----
<1> You need to declare just one csv dataset file. Database rider will take parent folder as dataset folder.
 """
    Then The database should be seeded with the dataset content before test execution