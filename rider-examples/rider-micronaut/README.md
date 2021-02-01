# Micronaut Data JPA Java Example

This example demonstrates Micronaut Data JPA for Java.

## Example Structure

* `src/main/java/example/domain` - Entities that map onto database tables
* `src/main/java/example/repositories` - Micronaut Data Repository interfaces
* `src/main/java/example/controllers` - Controllers that are injected with the repository interfaces
* `src/test/java/example` - JUnit 5 tests that test the example.

## Example Configuration

The example is configured to use an in-memory H2 database via `src/main/resources/application.yml`.

## Running the example

You can run the tests with either `./gradlew clean test` for Gradle or `./mvnw test` for Maven.

The application can be run with either `./gradlew run` or `./mvnw compile exec:exec`. 

Alternatively you can import the example into either IntelliJ, Visual Studio Code or Eclipse.

## Native Image

A native image can be built by installing GraalVM 19.1 or above and running the following for Gradle:

```bash
$ ./gradlew assemble 
$ native-image --no-server -cp build/libs/example-jpa-0.1-all.jar
```

Or for Maven:

```bash
$ ./mvnw package 
$ native-image --no-server -cp target/example-jpa-0.1.jar
```

 