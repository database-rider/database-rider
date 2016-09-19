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

Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/ScriptReplacementsIt.java#L18[found here^].


  Scenario: Seed database with groovy script in dataset

     #cukedoctor-discrete
    Given Groovy script engine is on test classpath
"""
[source,xml,indent=0]
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
include::../../../src/test/resources/datasets/yml/groovy-with-date-replacements.yml[]
----
<1> Groovy scripting is enabled by `groovy:` string.
"""

#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/ScriptReplacementsIt.java#L55[found here^].}
#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0,linenums]
----
include::../../../src/test/java/com/github/database/rider/core/ScriptReplacementsIt.java[tags=groovy]
----
"""
    Then Dataset script should be interpreted while seeding the database

  Scenario: Seed database with javascript in dataset

    NOTE: Javascript engine comes within JDK so no additional classpath dependency is necessary.


  #cukedoctor-discrete
    Given The following dataset
"""
----
include::../../../src/test/resources/datasets/yml/js-with-calc-replacements.yml[]
----
<1> Javascript scripting is enabled by `js:` string.
"""

#{TIP: Source code of the above example can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/ScriptReplacementsIt.java#L44[found here^].}
#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,linenums,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/ScriptReplacementsIt.java[tags=javascript-likes]
----
"""
    Then Dataset script should be interpreted while seeding the database