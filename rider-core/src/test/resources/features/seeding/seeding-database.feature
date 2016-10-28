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

* https://github.com/junit-team/junit4/wiki/Rules[JUnit rules^] (in JUnit4 tests);
* https://docs.jboss.org/weld/reference/latest/en-US/html_single/#interceptors[CDI interceptor^] (in `CDI` based tests)
* http://junit.org/junit5/docs/current/user-guide/#extensions[JUnit5 extension^].
 

  Scenario: Seeding database with `DBUnit Rule`

    JUnit4 integrates with DBUnit through a https://github.com/junit-team/junit4/wiki/Rules[JUnit rule^] called `DBUnitRule` which reads *@Dataset* annotations in order to prepare the database state using DBUnit behind the scenes.

    NOTE: The rule just needs a https://docs.oracle.com/javase/tutorial/jdbc/[JDBC^] connection in order to be created.

[discrete]
=== *Dependencies*

To use it add the following maven dependency:

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
<2> DBUnit rule is responsible for reading `@DataSet` annotation and prepare the database for each test.

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

    Scenario: Seed database with `DBUnit Interceptor`

    DBUnit CDIfootnote:[http://docs.oracle.com/javaee/6/tutorial/doc/giwhb.html[Contexts and dependency for the Java EE^]] integration is done through a https://docs.jboss.org/weld/reference/latest/en-US/html_single/#interceptors[CDI interceptor^] which reads `@DataSet` to prepare database in CDI tests.

[discrete]
=== *Dependencies*

To use this module just add the following maven dependency:

[source,xml]
----
<dependency>
       <groupId>com.github.database-rider</groupId>
       <artifactId>rider-cdi</artifactId>
include::../../../../pom.xml[tags=version]
       <scope>test</scope>
</dependency>
----

#cukedoctor-discrete
#{[IMPORTANT]}
#{======}
#{Your test itself must be a CDI bean to be intercepted. if you’re using https://deltaspike.apache.org/documentation/test-control.html[Deltaspike test control^] just enable the following property in `test/resources/META-INF/apache-deltaspike.properties`:}
#{----}
#{deltaspike.testcontrol.use_test_class_as_cdi_bean=true}
#{----}
#{======}
Given DBUnit interceptor is enabled in your test beans.xml:
  """
.src/test/resources/META-INF/beans.xml
[source,xml]
----
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">

       <interceptors>
              <class>com.github.database.rider.cdi.DBUnitInterceptorImpl</class>
       </interceptors>
</beans>
----

  """


#cukedoctor-discrete
And The following dataset

 """
.src/test/resources/dataset/yml/users.yml
----
include::../../../../rider-cdi/src/test/resources/datasets/yml/users.yml[]
----
 """

#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-cdi/src/test/java/com/github/database/rider/cdi/DBUnitCDIIt.java#L74[found here^].}
#cukedoctor-discrete
When The following test is executed:
 """
[source,java]
----
include::../../../../rider-cdi/src/test/java/com/github/database/rider/cdi/DBUnitCDIIt.java[tags=cdi-declaration]

include::../../../../rider-cdi/src/test/java/com/github/database/rider/cdi/DBUnitCDIIt.java[tags=seedDatabase]
----
<1> https://deltaspike.apache.org/documentation/test-control.html[CdiTestRunner^] is provided by https://deltaspike.apache.org[Apache Deltaspike^] but you should be able to use other CDI test runners.
<2> Needed to activate DBUnit interceptor

 """

Then The database should be seeded with the dataset content before test execution


Scenario: Seed database with `JUnit 5 extension`

  DBUnit is enabled in JUnit 5 tests through an http://junit.org/junit5/docs/current/user-guide/#extensions[extension^] named *DBUnitExtension*.

[discrete]
=== *Dependencies*

To use the extension just add the following maven dependency:

[source,xml]
----
<dependency>
     <groupId>com.github.dbunit-rules</groupId>
     <artifactId>junit5</artifactId>
include::../../../../pom.xml[tags=version]
     <scope>test</scope>
</dependency>
----

  #cukedoctor-discrete
Given The following dataset
 """
.src/test/resources/dataset/users.yml
----
include::../../../../rider-junit5/src/test/resources/datasets/users.yml[]
----
 """

#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-junit5/src/test/java/com/github/database/rider/junit5/DBUnitJUnit5It.java/#L24[found here^].}
#{[TIP]}
#{====}
#{Another way to activate DBUnit in JUnits 5 test is using *@DBRider* annotation (at method or class level):}
#{[source,java]}
#{----}
#{include::../../../../rider-junit5/src/test/java/com/github/database/rider/junit5/DBRiderAnnotationIt.java[tags=junit5-annotation]}
#{----}
#{<1> Shortcut for `@Test` and `@ExtendWith(DBUnitExtension.class)`}
#{====}
#cukedoctor-discrete
When The following junit5 test is executed

 """
[source,java]
----
include::../../../../rider-junit5/src/test/java/com/github/database/rider/junit5/DBUnitJUnit5It.java[tags=declaration;connectionField;test]
----
<1> Enables DBUnit.
<2> JUnit 5 runner;
<3> As JUnit5 requires *Java8* you can use lambdas in your tests;
<4> DBUnitExtension will get connection by reflection so just declare a field or a method with `ConnectionHolder` as return type.

 """

Then The database should be seeded with the dataset content before test execution
