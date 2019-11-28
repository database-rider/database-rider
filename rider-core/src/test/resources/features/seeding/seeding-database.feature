# order: 1
Feature: Seeding database
====
[quote]
____
In order to insert data into database before test execution
As a developer
I want to easily use DBUnit in JUnit tests.
____
====

Database Rider brings http://dbunit.sourceforge.net/[DBUnit^] to your http://junit.org/junit4/[JUnit tests] by means of:

* https://github.com/junit-team/junit4/wiki/Rules[JUnit rules^] (for JUnit4 tests);
* https://docs.jboss.org/weld/reference/latest/en-US/html_single/#interceptors[CDI interceptor^] (for `CDI` based tests)
* http://junit.org/junit5/docs/current/user-guide/#extensions[JUnit5 extension^] (for JUnit5 tests).


  Scenario: Seed database with `DBUnit Rule`

    JUnit4 integrates with DBUnit through a https://github.com/junit-team/junit4/wiki/Rules[JUnit rule^] called `DBUnitRule` which reads *@Dataset* annotations in order to prepare the database state using DBUnit behind the scenes.

    NOTE: The rule just needs a https://docs.oracle.com/javase/tutorial/jdbc/[JDBC^] connection in order to be created.

[discrete]
==== *Dependencies*

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
==== *Dependencies*

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
#{IMPORTANT: When using above configuration the JUnit `@Before` will not work as expected, see https://lists.apache.org/thread.html/60ae2ade9ff8c5588a53a138b64c94e505455185358c21f663a5fd33@%3Cusers.deltaspike.apache.org%3E[discussion here^].}
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

IMPORTANT: Since `v1.8.0` you can also use `com.github.database.rider.cdi.api.DBRider` annotation to enable database rider, both activate the DBUnitInterceptor.

 """

Then The database should be seeded with the dataset content before test execution


Scenario: Seed database with `JUnit 5 extension`

  DBUnit is enabled in JUnit 5 tests through an http://junit.org/junit5/docs/current/user-guide/#extensions[extension^] named *DBUnitExtension*.

[discrete]
==== *Dependencies*

To use the extension just add the following maven dependency:

[source,xml]
----
<dependency>
     <groupId>com.github.database-rider</groupId>
     <artifactId>rider-junit5</artifactId>
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


TIP: The same works for SpringBoot projects using JUnit5, see an example https://github.com/database-rider/database-rider/tree/master/rider-examples/spring-boot-dbunit-sample[project here^].


 """

Then The database should be seeded with the dataset content before test execution

  Scenario: Seeding database in BDD tests with `Rider Cucumber`

DBUnit enters the BDD world through a dedicated JUNit runner which is based on https://cucumber.io/[Cucumber^] and https://deltaspike.apache.org/[Apache DeltaSpike^].

This runner just starts CDI within your BDD tests so you just have to use <<_seed_database_with_code_dbunit_interceptor_code,Database Rider CDI interceptor>> on Cucumber steps, here is the so called Cucumber CDI runner declaration:

[source,java]
----
include::../../../../rider-examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactFeature.java[]
----


IMPORTANT: As cucumber doesn't work with JUnit Rules, see https://github.com/cucumber/cucumber-jvm/issues/393[this issue^], you won't be able to use Cucumber runner with _DBUnit Rule_, but you can use DataSetExecutor in `@Before`, see https://github.com/database-rider/database-rider/tree/master/rider-examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/withoutcdi[example here^].

[discrete]
==== *Dependencies*
Here is a set of maven dependencies needed by Database Rider Cucumber:

NOTE: Most of the dependencies, except CDI container implementation, are brought by Database Rider Cucumber module transitively.

[source,xml]
----
<dependency>
       <groupId>com.github.database-rider</groupId>
       <artifactId>rider-cucumber</artifactId>
include::../../../../pom.xml[tags=version]
       <scope>test</scope>
</dependency>
----

.Cucumber dependencies
[source,xml,indent=0]
----
include::../../../pom.xml[tags=cucumber-deps]
----
<1> You don't need to declare because it comes with Database Rider Cucumber module dependency.

.DeltaSpike and CDI dependency
[source,xml,indent=0]
----
include::../../../../rider-cdi/pom.xml[tags=deltaspike-cdi-deps]
----
<2> Also comes with Rider Cucumber.
<3> You can use CDI implementation of your choice.


#cukedoctor-discrete
Given The following feature
  """
----
include::../../../../rider-examples/jpa-productivity-boosters/src/test/resources/features/contacts.feature[]
----
  """


#cukedoctor-discrete
And The following dataset

 """
----
include::../../../../rider-examples/jpa-productivity-boosters/src/test/resources/datasets/contacts.yml[]
----
 """

#cukedoctor-discrete
And The following Cucumber test

 """
[source,java]
----
include::../../../../rider-examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactFeature.java[]
----
 """

#{TIP: Source code for the example above can be https://github.com/database-rider/database-rider/blob/master/rider-examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactSteps.java#L17[found here^].}
#cukedoctor-discrete
When The following cucumber steps are executed
 """
[source,java]
----
include::../../../../rider-examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactSteps.java[]
----
<1> Activates DBUnit CDI interceptor
<2> As the Cucumber cdi runner enables CDI, you can use injection into your Cucumber steps.
<3> Dataset is prepared before step execution by `@DBUnitInterceptor`.
 """

Then The database should be seeded with the dataset content before step execution
