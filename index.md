---
layout: landing
---


[**Database Rider**](https://github.com/database-rider/database-rider) integrates [DBUnit](http://dbunit.sourceforge.net/) and [JUnit](http://junit.org/) in order to make **database testing** a breeze!

 Here are some features:


* JUnit rule to integrate with DBUnit via annotations:

```
    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(jdbcConnection);

    @Test
    @DataSet(value = "datasets/yml/users.yml")
    public void shouldSeedDataSet(){
        //database is seed with users.yml dataset
    }
```

* [CDI](http://weld.cdi-spec.org/) integration via interceptor;

* JSON, YAML, XML, XLS, and CSV dataset format support;

* Configuration via annotations or yml files;

* Cucumber integration;

* Multiple database support;

* Date/time support in datasets;

* Scriptable datasets with groovy and javascript;

* Regular expressions in expected datasets;

* JUnit 5 integration;

* DataSet export;

* Connection leak detection;

* Lot of examples.
 


#### Documentation

Current version (under development) of documentation can be [found here]({{site.baseurl}}/latest/documentation.html).

Following is documentation for each released version:

{% for release in site.github.releases %}
  * [{{ release.tag_name }}]({{site.baseurl}}/{{ release.tag_name }}/documentation.html) ([PDF]({{site.baseurl}}/{{ release.tag_name }}/documentation.pdf))
{% endfor %}




#### Getting Started 

An introdutory guide with a sample application can be found here: https://database-rider.github.io/getting-started.

#### Examples

* [JPA productivity boosters](https://github.com/database-rider/database-rider/tree/master/rider-examples/jpa-productivity-boosters)

* [DBUnit Application Composer](https://github.com/database-rider/database-rider/tree/master/rider-examples/dbunit-tomee-appcomposer-sample)

* [jOOQ Flyway DBUnit](https://github.com/database-rider/database-rider/tree/master/rider-examples/jOOQ-DBUnit-flyway-example/)

* [SpringBoot Data DBUnit](https://github.com/database-rider/database-rider/tree/master/rider-examples/spring-boot-dbunit-sample/)


#### Credits
* Maintained by:
  * [Rafael M. Pestano](https://github.com/rmpestano)

[rmpestano]: https://github.com/rmpestano
