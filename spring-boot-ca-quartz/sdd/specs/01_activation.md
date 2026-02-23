# Feature Specification: Annotation-Driven Activation

| Metadata | Value |
| :--- | :--- |
| **Feature Name** | Annotation-Driven Activation |
| **Spec Version** | 1.0.0 |
| **Status** | Approved |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
The extension must be explicitly activated by the user using a dedicated annotation. This ensures that the carbon-aware scheduling logic does not affect standard Quartz behavior in applications where it is not desired.

## 2. Included Subfeatures
- **`@EnableCarbonAwareScheduling` Annotation**: A marker annotation to be placed on a `@Configuration` or `@SpringBootApplication` class.
- **Explicit Auto-Configuration Import**: Uses `@Import(CarbonAwareSchedulingAutoConfiguration.class)` to trigger the registration of the extension's components.
- **Conditional Loading**: The auto-configuration itself is conditional on:
    1.  The presence of the `@EnableCarbonAwareScheduling` annotation on a configuration class.
    2.  The property `carbon.aware.scheduling.enabled` being `true` (default: `true`).
    3.  If the annotation is present but the property is `false`, the extension remains dormant.

## 3. Explicitly NOT Included
- **Implicit Activation**: The extension will NOT use `META-INF/spring.factories` or `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for automatic discovery without the annotation.
- **Multi-Scheduler Activation**: Currently, the activation is global for the default Spring `Scheduler`. Activating carbon-aware scheduling for only one of multiple schedulers in the same context is NOT in scope.

## 4. Technical Details
- **Package**: `com.esentri.quartz.carbonaware.springboot`
- **Logic**:
    - The annotation `@EnableCarbonAwareScheduling` is meta-annotated with `@Import(CarbonAwareSchedulingAutoConfiguration.class)`.

## 5. Test Strategy
- Refer to [Test Specification](../test-specification.md): IT-1 (opt-in activation), IT-2 (dormant by default), IT-3 (explicit disable toggle).
