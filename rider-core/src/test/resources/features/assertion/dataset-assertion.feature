# order: 4
Feature: DataSet assertion
====
[quote]
____
In order to verify database state after test execution
As a developer
I want to assert database state with datasets.
____
====

TIP: Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/ExpectedDataSetIt.java#L23[found here^].


  Scenario: Assertion with yml dataset

     #cukedoctor-discrete
    Given The following dataset
"""
.expectedUsers.yml
----
include::../../../src/test/resources/datasets/yml/expectedUsers.yml[]
----
"""

#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/ExpectedDataSetIt.java[tags=expectedDeclaration;expected]
----
<1> Clear database before to avoid conflict with other tests.
"""
    Then Test must pass because database state is as in expected dataset.

  Scenario: Assertion with regular expression in expected dataset

       #cukedoctor-discrete
    Given The following dataset
"""
.expectedUsersRegex.yml
----
include::../../../src/test/resources/datasets/yml/expectedUsersRegex.yml[]
----
"""

  #cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/ExpectedDataSetIt.java[tags=expectedRegex]
----
"""
    Then Test must pass because database state is as in expected dataset.

  Scenario: Database assertion with seeding before test execution

       #cukedoctor-discrete
    Given The following dataset
"""
.user.yml
----
include::../../../src/test/resources/datasets/yml/user.yml[]
----
"""
    #cukedoctor-discrete
    And The following dataset
"""
.expectedUser.yml
----
include::../../../src/test/resources/datasets/yml/expectedUser.yml[]
----
"""

  #cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/ExpectedDataSetIt.java[tags=expectedWithSeeding]
----
"""
    Then Test must pass because database state is as in expected dataset.

  Scenario: Failing assertion

       #cukedoctor-discrete
    Given The following dataset
"""
.expectedUsers.yml
----
include::../../../src/test/resources/datasets/yml/expectedUsers.yml[]
----
"""

  #cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/ExpectedDataSetIt.java[tags=faillingExpected]
----
"""

  #cukedoctor-discrete
    Then Test must fail with following error:
"""
IMPORTANT: junit.framework.ComparisonFailure: value (table=USER, row=0, col=name) expected:<[]expected user1> but was:<[non ]expected user1>  at org.dbunit.assertion.JUnitFailureFactory.createFailure(JUnitFailureFactory.java:39) at org.dbunit.assertion.DefaultFailureHandler.createFailure(DefaultFailureHandler.java:97) at org.dbunit.assertion.DefaultFailureHandler.handle(DefaultFailureHandler.java:223) at ...
"""

  Scenario: Assertion using automatic transaction

   #cukedoctor-discrete
    Given The following dataset
"""
.expectedUsersRegex.yml
----
include::../../../src/test/resources/datasets/yml/expectedUsersRegex.yml[]
----
"""

#cukedoctor-discrete
    #{NOTE: `Transactional` attribute will make Database Rider start a transaction before test and commit the transaction *after* test execution but *before* expected dataset comparison.}
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/TransactionIt.java[tags=transaction]
----
"""
    Then Test must pass because inserted users are commited to database and database state matches expected dataset.