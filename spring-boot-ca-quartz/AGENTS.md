# Carbon-Aware Quartz Spring Boot Starter

This module provides a Spring Boot Starter for the carbon-aware Quartz scheduler. It allows you to easily integrate carbon-aware scheduling into your Spring Boot application using a simple annotation and type-safe properties.

## Features

- **Annotation-based Activation**: Just add `@EnableCarbonAwareScheduling` to your application.
- **Type-safe Configuration**: Configure everything under the `carbon.aware.scheduling` prefix.
- **Automatic Quartz Integration**: Automatically registers plugins and listeners with the Spring-managed `Scheduler`.
- **Spring Bean Awareness**: Automatically detects and uses `CarbonForecastApi` and `PersistenceApi` beans from the Spring context.

## Usage

### 1. Add Dependency

Add the starter to your `build.gradle`:

```gradle
implementation project(':spring-boot-ca-quartz')
```

### 2. Enable Carbon-Aware Scheduling

Add the `@EnableCarbonAwareScheduling` annotation to your main class or a configuration class:

```java
@SpringBootApplication
@EnableCarbonAwareScheduling
public class MyApplication {
    static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. Configuration

Configure the extension in your `application.yml`:

```yaml
carbon:
  aware:
    scheduling:
      enabled: true
      dry-run: false
      statistics:
        enabled: true
      open-data:
        enabled: true
        locations:
          - de
          - fr
```

## Advanced Usage

### Custom Forecast Provider

If you want to use a custom forecast provider, simply register it as a Spring Bean:

```java
@Bean
public CarbonForecastApi myCustomForecastApi() {
    return new MyCustomForecastApi();
}
```

The extension will automatically detect this bean and use it instead of the default OpenData provider.

### Custom Persistence Provider

Similarly, you can provide a custom persistence implementation:

```java
@Bean
public PersistenceApi myCustomPersistenceApi() {
    return new MyCustomPersistenceApi();
}
```

## Development Guidelines

### Spec-Driven Development (SDD)

This module follows a Spec-Driven Development approach. Every feature is first defined in its corresponding technical specification before implementation begins.

**Key Principles:**
- **Specs First**: Implementation only starts after the technical specification is finalized.
- **Traceability**: Every line of code can be traced back to a requirement in the specs.
- **Verification**: Tests are designed based on the specifications to ensure full compliance.

**SDD Artifacts:**
- **[Acceptance Criteria](sdd/acceptence-criteria.md)**: High-level functional and technical requirements.
- **[Architecture Design](sdd/architecture.md)**: High-level overview of component interaction and bridging logic.
- **[Technical Specifications](sdd/specs.md)**: Detailed design, sub-feature definitions, and Glossary.
- **[Test Specification](sdd/test-specification.md)**: Detailed mapping of requirements to test cases.

### Javadoc

All public classes and methods must have Javadoc.

### Build and Test

To build the module and run tests:

```bash
./gradlew :spring-boot-ca-quartz:build
```
