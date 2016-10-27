# order: 3
Feature: Manage database with Database Rider and JUnit 5
====
[quote]
____
In order to manage database state in http://junit.org/junit5/[JUnit 5^] integration tests
As a developer
I want to use DBUnit along side my JUnit 5 tests.
____
====

DBUnit is enabled in JUnit 5 tests through an http://junit.org/junit5/docs/current/user-guide/#extensions[extension^] named *DBUnitExtension*.

[discrete]
=== *Dependencies*

To use the extension just add the following maven dependency:

[source,xml,indent=0]
----
<dependency>
     <groupId>com.github.dbunit-rules</groupId>
     <artifactId>junit5</artifactId>
include::../../../../pom.xml[tags=version]
     <scope>test</scope>
</dependency>
----



Scenario: Seed database using Database Rider in JUnit5 tests

#cukedoctor-discrete
Given The following dataset
 """
.src/test/resources/dataset/users.yml
----
include::../../../../rider-junit5/src/test/resources/datasets/users.yml[]
----
 """

#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-junit5/src/test/java/com/github/database/rider/junit5/DBUnitJUnit5It.java/#L24[found here^].}
#cukedoctor-discrete
When The following junit5 test is executed

 """
[source,java]
----
include::../../../../rider-junit5/src/test/java/com/github/database/rider/junit5/DBUnitJUnit5It.java[tags=declaration;connectionField;test]
----
<1> Enables DBUnit;
<2> JUnit 5 runner;
<3> As JUnit5 requires *Java8* you can use lambdas in your tests;
<4> DBUnitExtension will get connection by reflection so just declare a field or a method with `ConnectionHolder` as return type.

 """

Then The database should be seeded with the dataset content before test execution
