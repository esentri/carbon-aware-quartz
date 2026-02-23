# SDD Task Board: Spring Boot Carbon-Aware Quartz

This task board tracks the implementation of the `spring-boot-ca-quartz` module following the Spec-Driven Development (SDD) process and the recommended implementation order.

## 1. Annotation-Driven Activation
*Spec: `docs/specs/01_activation.md`*

- [ ] Create `@EnableCarbonAwareScheduling` annotation.
- [ ] Create `CarbonAwareSchedulingAutoConfiguration`.
- [ ] Implement conditional loading (Annotation + `enabled` property).
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Unit tests for context loading (positive/negative).

## 2. Type-Safe Configuration Properties
*Spec: `docs/specs/02_configuration.md`*

- [ ] Create `CarbonAwareProperties` with all defined toggles.
- [ ] Configure `spring-boot-configuration-processor` in `build.gradle`.
- [ ] Implement property binding and default values.
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Unit tests for property mapping; Check `spring-configuration-metadata.json`.

## 3. Automated Quartz Integration
*Spec: `docs/specs/03_quartz_integration.md`*

### 3.1 Core Plugin & Listener
- [ ] Create `CarbonAwareSchedulerCustomizer` (`SchedulerFactoryBeanCustomizer`).
- [ ] Register `CarbonAwarePlugin`.
- [ ] Always attach `TimeShiftingTriggerListener` if enabled.
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Integration test; Verify listener present in `Scheduler`.

### 3.2 Conditional Statistics
- [ ] Attach `CarbonStatisticsTriggerListener` only if `statistics.enabled=true`.
- [ ] Propagate `dry-run` property to both listeners.
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Integration test; Verify statistics listener presence matches toggle.

## 4. Smart Dependency Management
*Spec: `docs/specs/04_dependency_management.md`*

- [ ] Implement detection logic for `CarbonForecastApi` and `PersistenceApi` beans.
- [ ] Implement `@Primary` support and property-based overrides.
- [ ] Create Spring-aware bridge for `CarbonAwarePlugin`.
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Integration test with custom beans and `@Primary`.

## 5. Integrated Forecast Providers (OpenData)
*Spec: `docs/specs/05_forecast_providers.md`*

- [ ] Implement conditional auto-config for `EnergyChartsForecastProvider`.
- [ ] Implement location initialization from properties.
- [ ] Ensure `OpenDataUpdateJob` is scheduled when enabled.
- [ ] **Verification**: Module compiles successful.
- [ ] **Verification**: Integration test; Verify provider bean and update job.

## 6. Hardening & DX Polishing
*Spec: `docs/specs.md` (Cross-Cutting)*

- [x] Validate all links and usage instructions in `AGENTS.md`.
- [x] Ensure `architecture.md` and `test-specification.md` are up to date.
- [x] Perform final full-build verification and cleanup.
- [x] **Verification**: Module compiles successful.
- [x] **Verification**: All tests pass; `build` task green; Javadoc generated.

## 7. Example Migration
- [ ] Update `examples/src/main/java/com/esentri/quartz/springboot/` to use the new starter.
- [ ] Verify that the example still works as expected with the simplified configuration.
- [ ] **Verification**: Run example; verify carbon-aware scheduling is active.
