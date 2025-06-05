# AGENTS.md

This repository hosts a Java 21 Spring Boot application built with Gradle.
Use the guidelines below when modifying the project.

## Testing

- Always run `./gradlew test` before committing. Fix any failures.
- Use JUnit 5 and Mockito for new tests. Place them under `src/test/java`.

## Building & Running

- Build or run with the provided Gradle wrapper (`./gradlew`).
- Run the server locally using `./gradlew bootRun` or by executing the jar from
  `./gradlew bootJar`.

## Project structure

- Application code lives in `src/main/java/ru/krotarnya/diasync`.
- Resources (static files, configuration) are under `src/main/resources`.
- Test code lives in `src/test/java/ru/krotarnya/diasync`
- Documentation resides in `README.MD`
