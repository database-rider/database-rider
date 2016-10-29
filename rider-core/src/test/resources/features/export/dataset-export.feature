# order: 7
Feature: DataSet export
====
[quote]
____
In order to easily create `dataset files`
As a developer
I want generate datasets based on database state.
____
====

Manual creation of datasets is a very error prone task. In order to export database state after test execution into datasets files one can use @ExportDataSet Annotation or use DataSetExporter component or even using a https://forge.jboss.org/[JBoss Forge^] addon.

TIP: Complete source code of examples below can be https://github.com/database-rider/database-rider/blob/master/rider-core/src/test/java/com/github/database/rider/core/exporter/ExportDataSetIt.java#L31[found here^].

Scenario: Export dataset with `@ExportDataSet` annotation

[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/exporter/ExportDataSetIt.java[tags=export-annotation]
----
<1> Used here just to seed database, you could insert data manually or connect to a database which already has data.

After above test execution all tables will be exported to a xml dataset.


NOTE: *XML, YML, JSON, XLS and CSV* formats are supported.

Scenario: Programmatic export

[source,java,indent=0]
----
include::../../../src/test/java/com/github/database/rider/core/exporter/ExportDataSetIt.java[tags=export-programmatically]
----

Scenario: Configuration

Following table shows all exporter configuration options:

[cols="3*", options="header"]
|===
|Name | Description | Default
|format| Exported dataset file format.| YML
|includeTables| A list of table names to include in exported dataset.| Default is empty which means *ALL tables*.
|queryList| A list of select statements which the result will used in exported dataset.| {}
|dependentTables| If true will bring dependent tables of declared includeTables.| false
|outputName| Name (and path) of output file.| ""
|===

Scenario: Export using DBUnit Addon

  https://github.com/database-rider/dbunit-addon[DBUnit Addon^] exports DBUnit datasets based on a database connection.

.Pre requisites

  You need https://forge.jboss.org/download[JBoss Forge^] installed in your IDE or available at command line.


.Installation

Use install addon from git command:

----
addon-install-from-git --url https://github.com/database-rider/dbunit-addon.git
----


.Usage

. Setup database connection
+
image::https://raw.githubusercontent.com/database-rider/dbunit-addon/master/setup_cmd.png["Setup command"]
. Export database tables into *YAML*, *JSON*, *XML*, *XLS* and *CSV* datasets.
+
image::https://raw.githubusercontent.com/database-rider/dbunit-addon/master/export_cmd.png["Export command"]

.Export configuration

\* `Format`: Dataset format.
\* `Include tables`: Name of tables to include in generated dataset. If empty all tables will be exported.
\* `Dependent tables`: If true will bring dependent included tables. Works in conjunction with `includeTables`.
\* `Query list`: A list of SQL statements which resulting rows will be used in generated dataset.
\* `Output dir`: directory to generate dataset.
\* `Name`: name of resulting dataset. Format can be ommited in dataset name.