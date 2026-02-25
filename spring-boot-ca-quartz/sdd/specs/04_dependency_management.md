# Feature Specification: Smart Dependency Management

| Metadata | Value |
| :--- | :--- |
| **Feature Name** | Smart Dependency Management |
| **Spec Version** | 1.0.0 |
| **Status** | Approved |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
The extension leverages the Spring ApplicationContext to automatically detect and use custom implementations of `CarbonForecastApi` and `PersistenceApi`. This allows users to provide their own logic by simply defining them as Spring Beans.

## 2. Included Subfeatures
- **Selection Precedence (highest → lowest)**:
    1. `@Primary` Spring bean (by type) if present.
    2. Property `carbon.aware.scheduling.<area>.bean-name` (explicit bean name).
    3. Property `carbon.aware.scheduling.<area>.impl-class` (FQCN), bridging to SPI behavior.
    4. Auto-detected single Spring bean by type.
    5. Default/SPI fallback (as implemented by `CarbonAwarePlugin`).
- **Smart Detection**:
    - Auto-scanning for `CarbonForecastApi` and `PersistenceApi` implementations in the Spring context.
- **SPI Fallback**:
    - If no Spring Bean is found, the system falls back to standard Java SPI discovery or properties-based class loading in `CarbonAwarePlugin`.
- **Bridging Mechanism**:
    - A Spring-aware bridge that passes the ApplicationContext-managed beans to the Quartz-managed plugin.

## 3. Explicitly NOT Included
- **Runtime Discovery of non-Spring beans**: The feature only considers beans registered within the Spring ApplicationContext. It will not look for SPI services in the classpath (unless bridged via the `custom-*-class` properties).
- **Multiple Implementation Usage**: Supporting multiple forecast APIs or persistence APIs simultaneously is NOT in scope.

## 4. Technical Details
- **Implementation**: The `CarbonAwareSchedulingAutoConfiguration` inspects the `ApplicationContext` and the override properties defined in `02_configuration.md` to resolve providers.
- **Areas**: `<area>` is either `forecast` (for `CarbonForecastApi`) or `persistence` (for `PersistenceApi`).
- **Bridging Logic**: If a bean is found/selected, it is passed through a bridge so that `CarbonAwarePlugin` and listeners use the Spring-managed instance instead of SPI-loaded ones.
- **Failure Modes**: If multiple beans exist without `@Primary` and no override properties are set, fail fast with a clear message indicating how to resolve via `@Primary` or properties.

## 5. Test Strategy
- Refer to [Test Specification](../test-specification.md): IT-5 (bean awareness & overrides), NT-1 (ambiguous beans).
