# order: 8
Feature: MetaDataSet
====
[quote]
____
In order to reuse datasets
As a developer
I want to create a custom annotation which holds my dataset and use it among tests.
____
====

TIP: Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/MetaDataSetIt.java#L21[found here^].

TIP: See https://github.com/database-rider/database-rider/blob/master/rider-spring/src/test/java/com/github/database/rider/spring/dataset/MetaDataSetIT.java[Rider Spring^], https://github.com/database-rider/database-rider/blob/master/rider-junit5/src/test/java/com/github/database/rider/junit5/MetaDataSetIt.java[JUnit5^] and https://github.com/database-rider/database-rider/blob/master/rider-cdi/src/test/java/com/github/database/rider/cdi/MetaDataSetIt.java[CDI^] examples. 

  Scenario: Class level metadataset

#cukedoctor-discrete
    Given The following metataset annotation
"""
.MetaDataSet.java
----
include::../../../src/test/java/com/github/database/rider/core/api/dataset/MetaDataSet.java[]
----
"""

#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/MetaDataSetIt.java[tags=declaration;class-level]
}
----

"""
    Then Test must use dataset declared in `MetaDataSet` annotation.

  Scenario: Method level metadaset
  
#cukedoctor-discrete
    Given The following metataset annotation
"""
.MetaDataSet.java
----
include::../../../src/test/java/com/github/database/rider/core/api/dataset/AnotherMetaDataSet.java[]
----
"""

#cukedoctor-discrete
    When The following test is executed:
"""
[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/MetaDataSetIt.java[tags=declaration;method-level]
}
----

"""
    Then Test must use dataset declared in `AnotherMetaDataSet` annotation. 

  