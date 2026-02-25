# Project Setup and Information

This document provides information regarding the project's setup, build process, and technology stack.

## Tech Stack

* **Java 17**: The project uses Java 17 as specified in the Gradle toolchain.
* **Gradle 8.11**: Used for build automation and dependency management.
* **Libraries**:
    * **Quartz (2.5.0)**: Main scheduling library.
    * **SLF4J (2.0.17)**: Logging facade.
    * **JUnit Jupiter (5.11.4)**: Testing framework for unit tests.

## Project Structure

* `quartz/`: Contains the core carbon-aware quartz scheduler implementation.
    * `src/main/java/com/esentri/quartz/carbonaware/`: Implementation of carbon-aware plugins, listeners, and triggers.
* `examples/`: Contains various example applications showcasing the usage of the carbon-aware scheduler.
    * `src/main/java/com/esentri/quartz/example1/`: Basic time-shifting example.
    * `src/main/java/com/esentri/quartz/springboot/`: Spring Boot integration example.
* `gradle/`: Gradle wrapper.
* `dashboards/`: Grafana and Docker Compose configurations for monitoring.

## Build and Run Instructions

### Build the project

To build the project and run tests, use the following command:

```bash
./gradlew build
```

### Run tests

To execute the unit tests:

```bash
./gradlew test
```

## Comments & Javadocs

- Always write Javadoc to every public method, class or attribute.
- Do not write Javadoc to private attributes or methods except for methods which contain complex logic.

## AGENTS.md Usage

The project contains several `AGENTS.md` files:

1. **Root `AGENTS.md`**: Provides high-level project information and setup.
2. **Module-specific `AGENTS.md`**: Might be provided in any arbitrary nesting.
