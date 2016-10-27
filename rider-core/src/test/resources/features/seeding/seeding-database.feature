# order: 1
Feature: Seeding database
====
[quote]
____
In order to insert data into database
As a developer
I want to easily use DBUnit in JUnit tests.
____
====

Database Rider brings http://dbunit.sourceforge.net/[DBunit^] to your http://junit.org/junit4/[JUnit tests] by means of:

* https://github.com/junit-team/junit4/wiki/Rules[JUnit rules^] (in JUnit4);
* https://docs.jboss.org/weld/reference/latest/en-US/html_single/#interceptors[CDI interceptor^] (in `CDI` based tests)
* http://junit.org/junit5/docs/current/user-guide/#extensions[extension^] (in case of `JUnit5`).
 

  Scenario: Seed database in a JUnit 4 test

    JUnit4 is integrated with DBUnit by means of a https://github.com/junit-team/junit4/wiki/Rules[JUnit rule^] called `DBUnitRule` which just needs a https://docs.oracle.com/javase/tutorial/jdbc/[JDBC^] connection in order to be created.

    This rule just reads *@Dataset* annotation in order to prepare the database state using DBUnit behind the scenes.


[discrete]
=== *Dependencies*

To use it just add the following maven dependency:

[source,xml]
----
<dependency>
       <groupId>com.github.database-rider</groupId>
       <artifactId>rider-core</artifactId>
include::../../../../pom.xml[tags=version]
       <scope>test</scope>
</dependency>
----



    #cukedoctor-discrete
    Given The following junit rules
    """
[source,java]
----
@RunWith(JUnit4.class)
public class DatabaseRiderIt {
include::../../../src/test/java/com/github/database/rider/core/DatabaseRiderIt.java[tags=rules]
}
----
<1> `EntityManagerProvider` is a simple Junit rule that creates a JPA entityManager for each test. DBUnit rule don’t depend on EntityManagerProvider, it only needs a *JDBC connection*.
<2> DBUnit rule responsible for reading `@DataSet` annotation and prepare the database for each test.

    """

#cukedoctor-discrete
    And The following dataset

 """
.src/test/resources/dataset/yml/users.yml
----
include::../../../src/test/resources/datasets/yml/users.yml[]
----
 """

#cukedoctor-discrete
#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/DatabaseRiderIt.java/#L31[found here^].}
    When The following test is executed:
 """
[source,java]
----
include::../../../src/test/java/com/github/database/rider/core/DatabaseRiderIt.java[tags=seedDatabase]
----
 """
    Then The database should be seeded with the dataset content before test execution
