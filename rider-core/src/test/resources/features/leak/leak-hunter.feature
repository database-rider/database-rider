# order: 6
Feature: Database connection leak detection
====
[quote]
____
In order to find JDBC connection leaks
As a developer
I want to make Database Rider monitor connections during tests execution.
____
====

Leak hunter is a Database Rider component, based on https://vladmihalcea.com/2016/07/12/the-best-way-to-detect-database-connection-leaks/[this blog post^], which counts open jdbc connections before and after test execution.

TIP: Complete source code of example below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/LeakHunterIt.java#L19[found here^].

Scenario: Detecting connection leak

[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/LeakHunterIt.java[tags=leak-hunter-declare;find-leak;ceate-leak]
----
<1> Enables connection leak detection.

NOTE: If number of connections after test execution are greater than before then a *LeakHunterException* will be raised.