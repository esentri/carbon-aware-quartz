# Test Specification: Spring Boot Carbon-Aware Quartz

This document defines the test cases required to verify the `spring-boot-ca-quartz` module against its Acceptance Criteria.

## 1. Unit Tests (Logic & Property Mapping)

### UT-1: Property Binding
- **Objective**: Verify that properties under `carbon.aware.scheduling` are correctly bound to the `CarbonAwareProperties` bean.
- **Preconditions**: Spring Boot context with a `PropertySource`.
- **Test Steps**:
    1. Set all properties in `application.yml`.
    2. Assert that the `CarbonAwareProperties` bean has the expected values.
- **Expected Outcome**: All properties (enabled, dry-run, statistics, open-data, overrides) match the input.

### UT-2: Default Values
- **Objective**: Ensure that default values are correctly applied when properties are missing.
- **Expected Outcome**:
    - `enabled`: `true`
    - `dry-run`: `false`
    - `statistics.enabled`: `false`
    - `open-data.enabled`: `false`

## 2. Integration Tests (Auto-Configuration & Context)

### IT-1: Opt-In Activation (Positive)
- **Objective**: Verify that `@EnableCarbonAwareScheduling` activates the extension.
- **Preconditions**: `@EnableCarbonAwareScheduling` on a configuration class.
- **Assertions**:
    - `CarbonAwareSchedulingAutoConfiguration` bean exists.
    - `CarbonAwareProperties` bean exists.
    - `CarbonAwareSchedulerCustomizer` bean exists.

### IT-2: Dormant by Default (Negative)
- **Objective**: Verify that without the annotation, no carbon-aware beans are registered.
- **Preconditions**: Standard Spring Boot application without the annotation.
- **Assertions**:
    - `CarbonAwareSchedulingAutoConfiguration` does NOT exist.
    - No `carbon.aware.*` beans in context.

### IT-3: Explicit Disable Toggle
- **Objective**: Verify that `carbon.aware.scheduling.enabled=false` overrides the annotation.
- **Preconditions**: Annotation present, property set to `false`.
- **Assertions**: Beans are not registered (except for properties if they are used as a condition).

### IT-4: Quartz Listener Attachment
- **Objective**: Verify that listeners are correctly attached to the Quartz `Scheduler`.
- **Assertions**:
    - `TimeShiftingTriggerListener` is present in `scheduler.getListenerManager().getTriggerListeners()`.
    - `CarbonStatisticsTriggerListener` is present ONLY if `statistics.enabled=true`.

### IT-5: Bean Awareness & Overrides
- **Objective**: Verify selection precedence for API implementations.
- **Scenarios**:
    - **Single Bean**: Define one `CarbonForecastApi` bean; verify it's used.
    - **@Primary**: Define two beans, one with `@Primary`; verify primary is used.
    - **Property Override**: Define two beans, use `forecast.bean-name` to select one; verify selected is used.
    - **SPI Fallback**: Define no beans; verify fallback to SPI behavior.

### IT-6: OpenData Turnkey Integration
- **Objective**: Verify that enabling OpenData schedules the update job.
- **Assertions**:
    - `OpenDataUpdateJob` is present in the `Scheduler`.
    - `EnergyChartsForecastProvider` is initialized.

## 3. Negative & Edge Case Tests

### NT-1: Ambiguous API Beans
- **Objective**: Verify fail-fast behavior when multiple beans exist without clear selection.
- **Preconditions**: Two `CarbonForecastApi` beans, no `@Primary`, no property override.
- **Expected Outcome**: Application fails to start with a descriptive exception.

### NT-2: Missing SPI Provider
- **Objective**: Verify behavior when no Spring bean exists and no SPI provider is found.
- **Expected Outcome**: Clear error message from `CarbonAwarePlugin` initialization.

## 4. Verification Checklist (Traceability)

| AC ID | Test Case(s) | Status |
| :--- | :--- | :--- |
| AC 1.1 | IT-1 | |
| AC 1.3 | IT-2, IT-3 | |
| AC 2.1 | IT-4 | |
| AC 2.2 | IT-4 | |
| AC 3.1 | UT-1, UT-2 | |
| AC 4.1 | IT-5 | |
| AC 4.2 | IT-5 | |
| AC 4.3 | IT-5 | |
| AC 5.4 | All | |
