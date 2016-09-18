# order: 3
Feature: Manage database with Database Rider Cucumber
====
[quote]
____
In order to manage database state in `BDD` tests
As a BDD developer
I want to use DBUnit along side my BDD tests.
____
====

DBUnit enters the BDD world through a dedicated JUNit runner which is based on https://cucumber.io/[Cucumber^] and https://deltaspike.apache.org/[Apache DeltaSpike^].

This runner just starts CDI within your BDD tests so you just have to use <<Manage database with Database Rider CDI,Database Rider CDI interceptor>> on Cucumber steps, here is the so called Cucumber CDI runner declaration:

[source,java]
----
include::../../src/test/java/com/github/database/rider/bdd/DatabaseRiderBdd.java[]
----


IMPORTANT: As cucumber doesn't work with JUnit Rules, see https://github.com/cucumber/cucumber-jvm/issues/393[this issue^], you won't be able to use Cucumber runner with _Database Rider Core_ because its based on JUnit rules, but you can use DataSetExecutor in `@Before`, see https://github.com/database-rider/database-rider/tree/master/examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/withoutcdi[example here^].

[discrete]
=== *Dependencies*
Here is a set of maven dependencies needed by Database Rider Cucumber:

NOTE: Most of the dependencies, except CDI container implementation, are bring by Database Rider Cucumber module transitively.

[source,xml,indent=0]
----
<dependency>
     <groupId>com.github.dbunit-rules</groupId>
     <artifactId>cucumber</artifactId>
include::../../../pom.xml[tags=version]
     <scope>test</scope>
</dependency>
----

.Cucumber dependencies
[source,xml,indent=0]
----
include::../../../cucumber/pom.xml[tags=cucumber-deps]
----
<1> You don't need to declare because it comes with Database Rider Cucumber module dependency.

.DeltaSpike and CDI dependency
[source,xml,indent=0]
----
include::../../../cucumber/pom.xml[tags=deltaspike-cdi-deps]
----
<2> Also comes with DBUit Rules Cucumber.
<3> You can use CDI implementation of your choice.


To use this module just add the following maven dependency:


=====

Scenario: Seed database using Database Rider in Cucumber tests

#cukedoctor-discrete
Given The following feature
  """
----
include::../../../examples/jpa-productivity-boosters/src/test/resources/features/contacts.feature[]
----
  """


#cukedoctor-discrete
And The following dataset

 """
----
include::../../../examples/jpa-productivity-boosters/src/test/resources/datasets/contacts.yml[]
----
 """

#cukedoctor-discrete
And The following Cucumber test

 """
[source,java,linenums]
----
include::../../../examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactFeature.java[]
----
 """

#{TIP: Source code for the example above can be https://github.com/database-rider/database-rider/blob/master/examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactSteps.java#L16[found here^].}
#cukedoctor-discrete
When The following cucumber steps are executed
 """
[source,java,linenums]
----
include::../../../examples/jpa-productivity-boosters/src/test/java/com/github/database/rider/examples/cucumber/ContactSteps.java[]
----
<1> As the Cucumber cdi runner enables CDI, you can use injection into your Cucumber steps.
<2> Here we use the Database Rider CDI interceptor to seed the database before step execution.
 """

Then The database should be seeded with the dataset content before step execution
