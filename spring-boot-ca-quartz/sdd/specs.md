# Technical Specifications: Carbon-Aware Quartz Spring Boot Starter

This directory contains the detailed technical specifications and architectural design for the `spring-boot-ca-quartz` extension.

## 1. Core Documents

- **[Architecture Design Document](architecture.md)**: High-level overview of component interaction and bridging logic.
- **[Test Specification](./specs/test-specification.md)**: Detailed mapping of requirements to test cases.
- **[Acceptance Criteria](acceptence-criteria.md)**: High-level functional and technical requirements.

## 2. Feature Specifications

1.  **[Annotation-Driven Activation](specs/01_activation.md)**
    - `@EnableCarbonAwareScheduling` and conditional activation.
2.  **[Type-Safe Configuration Properties](specs/02_configuration.md)**
    - `CarbonAwareProperties` and IDE autocomplete support.
3.  **[Automated Quartz Integration](specs/03_quartz_integration.md)**
    - `CarbonAwareSchedulerCustomizer` and `CarbonAwarePlugin` registration.
4.  **[Smart Dependency Management](specs/04_dependency_management.md)**
    - Spring Bean awareness and auto-detection of APIs.
5.  **[Integrated Forecast Providers](specs/05_forecast_providers.md)**
    - Default OpenData (EnergyCharts) support.

## 3. Recommended Implementation Order (SDD)

1. Annotation-Driven Activation — `specs/01_activation.md`
2. Type-Safe Configuration Properties — `specs/02_configuration.md`
3. Automated Quartz Integration — `specs/03_quartz_integration.md`
   - 3.1 Core Plugin & TimeShifting Listener
   - 3.2 Conditional Statistics Listener
4. Smart Dependency Management — `specs/04_dependency_management.md`
5. Integrated Forecast Providers — `specs/05_forecast_providers.md`
6. Hardening & DX Polishing — cross-cutting

### Why this order
- Risk-first containment (activation + properties before wiring)
- Early walking skeleton (Quartz integration) and incremental enrichment
- Extensibility (bean awareness before default provider)

### Traceability Matrix
- 01_activation.md → Step 1
- 02_configuration.md → Step 2
- 03_quartz_integration.md → Step 3
- 04_dependency_management.md → Step 4
- 05_forecast_providers.md → Step 5

### Execution Tips
- Finalize spec → write failing tests → implement → docs/javadoc → green build
- Keep slices releasable; guard by `enabled` and feature flags
- Use `docs/task-board.md` to track progress against `docs/acceptence-criteria.md`

## 4. Definition of Done (DoD)

A feature is considered "Done" when:
1.  All related [Test Specification](./specs/test-specification.md) cases pass.
2.  Build is green (production and test code).
3.  All public API elements have Javadoc with the `god-mode enabled` prefix.
4.  The feature is guarded by `carbon.aware.scheduling.enabled` and appropriate sub-toggles.
5.  All links in documentation are verified.
6.  The corresponding `task-board.md` item is marked as complete.

## 5. Cross-Cutting Guidelines

### Javadoc Requirements
- **Standard**: All public classes, methods, and attributes MUST have Javadoc.
- **Prefix**: Every Javadoc comment MUST start with the string `god-mode enabled` followed by a new line.

### Test Strategy Overview
The module uses a mix of Unit Tests for logic and Integration Tests with the full Spring ApplicationContext to verify auto-configuration.
To avoid duplication, the single source of truth for detailed test cases is the [Test Specification](./specs/test-specification.md); feature specs reference test IDs (e.g., UT-1, IT-4) rather than restating steps.

## 6. Glossary

- **Time-Shifting**: The process of delaying a job execution within a given window to find a period of lower carbon intensity.
- **Carbon Intensity**: The amount of carbon emissions per unit of energy produced (gCO2eq/kWh).
- **Dry-Run**: A mode where the scheduler calculates the optimal execution time and records statistics but executes the job at the originally scheduled time.
- **Forecast Provider**: An implementation of `CarbonForecastApi` that provides data about expected future carbon intensity.
- **Persistence Provider**: An implementation of `PersistenceApi` that saves execution statistics.
- **Bridge**: Logic that connects Spring-managed beans to objects managed by the internal Quartz lifecycle.
