# SDD Task Board: Spring Boot Carbon-Aware Quartz

This task board tracks the implementation of the `spring-boot-ca-quartz` module following the Spec-Driven Development (SDD) process and the recommended implementation order.

## 1. Annotation-Driven Activation
*Spec: `docs/specs/01_activation.md`*

- [x] Create `@EnableCarbonAwareScheduling` annotation.
- [x] Create `CarbonAwareSchedulingAutoConfiguration`.
- [x] Implement conditional loading (Annotation + `enabled` property).
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Unit tests for context loading (positive/negative).

## 2. Type-Safe Configuration Properties
*Spec: `docs/specs/02_configuration.md`*

- [x] Create `CarbonAwareProperties` with all defined toggles.
- [x] Configure `spring-boot-configuration-processor` in `build.gradle`.
- [x] Implement property binding and default values.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Unit tests for property mapping; Check `spring-configuration-metadata.json`.

## 3. Automated Quartz Integration
*Spec: `docs/specs/03_quartz_integration.md`*

### 3.1 Core Plugin & Listener
- [x] Create `CarbonAwareSchedulerCustomizer` (`SchedulerFactoryBeanCustomizer`).
- [x] Register `CarbonAwarePlugin`.
- [x] Always attach `TimeShiftingTriggerListener` if enabled.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Integration test; Verify listener present in `Scheduler`.

### 3.2 Conditional Statistics
- [x] Attach `CarbonStatisticsTriggerListener` only if `statistics.enabled=true`.
- [x] Propagate `dry-run` property to both listeners.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Integration test; Verify statistics listener presence matches toggle.

## 4. Smart Dependency Management
*Spec: `docs/specs/04_dependency_management.md`*

- [x] Implement detection logic for `CarbonForecastApi` and `PersistenceApi` beans.
- [x] Implement `@Primary` support and property-based overrides.
- [x] Create Spring-aware bridge for `CarbonAwarePlugin`.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Integration test with custom beans and `@Primary`.

## 5. Integrated Forecast Providers (OpenData)
*Spec: `docs/specs/05_forecast_providers.md`*

- [x] Implement conditional auto-config for `EnergyChartsForecastProvider`.
- [x] Implement location initialization from properties.
- [x] Ensure `OpenDataUpdateJob` is scheduled when enabled.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: Integration test; Verify provider bean and update job.

## 6. Hardening & DX Polishing
*Spec: `docs/specs.md` (Cross-Cutting)*

- [x] Validate all links and usage instructions in `AGENTS.md`.
- [x] Ensure `architecture.md` and `test-specification.md` are up to date.
- [x] Perform final full-build verification and cleanup.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: All tests pass; `build` task green; Javadoc generated.

## 7. Example Migration
- [x] Update `examples/src/main/java/com/esentri/quartz/springboot/` to use the new starter.
- [x] Verify that the example still works as expected with the simplified configuration.
- [x] **Verification**: Run example; verify carbon-aware scheduling is active.

## 8. Release
- [x] Create `CHANGELOG.md`.
- [x] Create `README.md`. The readme should support users, who wants to integrate the module into their project.
- [x] Perform final full-build verification and cleanup.