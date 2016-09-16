# order: 4
Feature: Dynamic data using scritable datasets
====
[quote]
____
In order to have dynamic data in datasets
As a developer
I want to use scripts in DBUnit datasets.
____
====

Scritable datasets are backed by JSR 223.footnote:[Scripting for the Java Platform, for more information access the official https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/api.html[docs here^]].


  Scenario: Seed database with groovy script in dataset

     #cukedoctor-discrete
    Given Groovy script engine is on test classpath
"""
----
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <version>2.4.6</version>
    <scope>test</scope>
</dependency>
----
"""

    #cukedoctor-discrete
    And The following dataset
"""
----
include::../../../core/src/test/resources/datasets/yml/groovy-with-date-replacements.yml[]
----
<1> Groovy scripting is enabled by `groovy:` string.
"""


#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0,linenums]
----
include::../../../core/src/test/java/com/github/dbunit/rules/ScriptReplacementsIt.java[tags=groovy]
----
"""
    Then Dataset script should be interpreted while seeding the database

  Scenario: Seed database with javascript in dataset

    NOTE: Javascript engine comes within JDK so no additional classpath dependency is necessary.


  #cukedoctor-discrete
    Given The following dataset
"""
----
include::../../../core/src/test/resources/datasets/yml/js-with-calc-replacements.yml[]
----
<1> Javascript scripting is enabled by `js:` string.
"""


#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,linenums,indent=0]
----
include::../../../core/src/test/java/com/github/dbunit/rules/ScriptReplacementsIt.java[tags=javascript-likes]
----
"""
    Then Dataset script should be interpreted while seeding the database