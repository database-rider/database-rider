# order: 1
Feature: Manage database with Database Rider Core
====
[quote]
____
In order to manage database state in JUnit tests
As a developer
I want to use DBUnit in my tests.
____
====

Database Rider Core module brings http://dbunit.sourceforge.net/[DBunit^] to your unit tests via https://github.com/junit-team/junit4/wiki/Rules[JUnit rules^].

[discrete]
=== *Dependencies*

To use it just add the following maven dependency:

[source,xml,indent=0]
----
<dependency>
     <groupId>com.github.database-rider</groupId>
     <artifactId>rider-core</artifactId>
include::../../../pom.xml[tags=version]
     <scope>test</scope>
</dependency>
----

  Scenario: Seed database using yml dataset

    #cukedoctor-discrete
    Given The following junit rules
    """
[source,java]
----
@RunWith(JUnit4.class)
public class DBUnitRulesIt {
include::../../src/test/java/com/github/database/rider/core/DBUnitRulesIt.java[tags=rules]
}
----
<1> https://github.com/database-rider/database-rider/blob/master/rider-core/src/main/java/com/github/database/rider/core/util/EntityManagerProvider.java[EntityManagerProvider^] is a simple Junit rule that creates a JPA entityManager for each test. DBUnit rule don’t depend on EntityManagerProvider, it only needs a *JDBC connection*.
<2> DBUnit rule responsible for reading `@DataSet` annotation and prepare the database for each test.

    """

#cukedoctor-discrete
    And The following dataset

 """
.src/test/resources/dataset/yml/users.yml
----
include::../../src/test/resources/datasets/yml/users.yml[]
----
 """

#cukedoctor-discrete
#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/core/src/test/java/com/github/database/rider/core/DBUnitRulesIt.java/#L31[found here^].}
    When The following test is executed:
 """
[source,java]
----
include::../../src/test/java/com/github/database/rider/core/DBUnitRulesIt.java[tags=seedDatabase]
----
 """
    Then The database should be seeded with the dataset content before test execution