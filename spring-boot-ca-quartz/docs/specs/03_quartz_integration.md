# Feature Specification: Automated Quartz Integration

| Metadata | Value |
| :--- | :--- |
| **Feature Name** | Automated Quartz Integration |
| **Spec Version** | 1.0.0 |
| **Status** | Approved |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
Automatically configures the Quartz `Scheduler` to use the `CarbonAwarePlugin` and necessary listeners by integrating with the Spring Boot Quartz auto-configuration.

## 2. Included Subfeatures
- **Step 3.1: Core Plugin & Listener Registration**:
    - Automatically adds `CarbonAwarePlugin` to Quartz.
    - Ensures `TimeShiftingTriggerListener` is always attached when the extension is active.
- **Step 3.2: Conditional Statistics**:
    - Conditionally attaches `CarbonStatisticsTriggerListener` based on `statistics.enabled`.
    - Handles `dry-run` propagation to both listeners.
- **`CarbonAwareSchedulerCustomizer`**: Implements `SchedulerFactoryBeanCustomizer` to inject these configurations.

## 3. Explicitly NOT Included
- **Manual Listener Attachment**: Users should not need to manually add `TimeShiftingTriggerListener` or `CarbonStatisticsTriggerListener` when using this extension.
- **Custom Plugin Support**: This feature only manages the `CarbonAwarePlugin`. Other Quartz plugins must be configured via standard Spring/Quartz mechanisms.

## 4. Technical Details
- **Interface**: `org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer`
- **Logic**:
    - In `customize(SchedulerFactoryBean factoryBean)`, it sets Quartz properties like `org.quartz.plugin.carbon-aware-plugin.class`.
    - It ensures that the `CarbonAwarePlugin` is initialized with values from `CarbonAwareProperties`.

## 5. Test Strategy
- Refer to [Test Specification](../test-specification.md): IT-4 (Quartz listener attachment).
