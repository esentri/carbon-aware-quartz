# Examples Module

This module contains various example applications showcasing the features and integrations of the carbon-aware scheduler.

## Key Examples

1.  **Basic Time-Shifting (Example 1)**: Demonstrates the core functionality of scheduling a job that can be time-shifted to periods of lower grid carbon intensity.
2.  **Dry-Run Mode (Example 2)**: Shows how to use the dry-run feature, where the scheduler calculates the optimal execution time without actually delaying the job, allowing for verification of the logic.
3.  **Statistics Recording (Example 3)**: Illustrates how to record and persist carbon emission statistics, which can be visualized using dashboards.
4.  **Open Data Provider (Example 4)**: Shows the usage of the default `OpenDataForecastClient` to fetch carbon intensity forecasts from open data sources (e.g., Energy-Charts).
5.  **Apache Camel Integration**: Demonstrates how to integrate the carbon-aware scheduler with Apache Camel routes using the `camel-quartz` component.
6.  **Spring Boot Integration**: Provides a complete Spring Boot application example with JDBC jobstore and carbon statistics persistence, supporting both H2 and PostgreSQL.

## Module Dependencies

Beyond the core `quartz` module, these examples utilize:
*   **Apache Camel (4.8.3)**: For the Camel integration example.
*   **Spring Boot (3.5.3)**: For the Spring Boot application and JDBC/JPA examples.
*   **Lombok (1.18.38)**: To reduce boilerplate code in the examples.
*   **H2 Database / PostgreSQL**: Used for persistent storage in the Spring Boot examples.
*   **Logback (1.5.18)**: For logging.

## How to Run the Examples

The examples can be executed via the following Gradle tasks:

*   **Example 1**: `./gradlew :examples:runExample1`
*   **Example 2**: `./gradlew :examples:runExample2`
*   **Example 3**: `./gradlew :examples:runExample3`
*   **Example 4**: `./gradlew :examples:runExample4`
*   **Camel Example**: `./gradlew :examples:runCamelExample`
*   **Spring Boot (H2)**: `./gradlew :examples:runExampleSpringBoot-h2`
*   **Spring Boot (Postgres)**: `./gradlew :examples:runExampleSpringBoot-postgres`

Note: For the Postgres example, ensure a PostgreSQL instance is running and configured as specified in `application-postgres.properties`.
