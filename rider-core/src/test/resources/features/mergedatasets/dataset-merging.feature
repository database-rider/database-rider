# order: 9
Feature: DataSet merging
====
[quote]
____
In order to reuse dataset configuration between test methods
As a developer
I want merge `class level` with `test level` dataset configuration
____
====

TIP: Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/MergeDataSetsIt.java#L25[found here^].

TIP: See https://github.com/database-rider/database-rider/blob/master/rider-junit5/src/test/java/com/github/database/rider/junit5/MergeDataSetsJUnit5It.java#L20[Rider JUnit5^] and https://github.com/database-rider/database-rider/blob/master/rider-cdi/src/test/java/com/github/database/rider/cdi/MergeDataSetsCDIIt.java#L26[CDI^] examples. 

  Scenario: Merging datasets

#cukedoctor-discrete
    Given The following class level dataset configuration
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/MergeDataSetsIt.java[tags=declaration]
}
----
<1> Enables dataset merging so @DataSet declared on test class will be merged with test/method one. 

"""

#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/MergeDataSetsIt.java[tags=test-method;after-test]
----
"""

#cukedoctor-discrete
    Then Test and method dataset configuration will be merged in one dataset

"""
IMPORTANT: Only array properties such as `value` and `executeScriptsBefore` of @DataSet will be merged.

WARNING: Class level dataset configuration will come before method level if a property is defined in both datasets, like `executeStatementsBefore` in example above.

TIP: You can enable dataset merging for all tests with 'mergeDataSets=true` on `dbunit.yml` configuraton file.
"""



  