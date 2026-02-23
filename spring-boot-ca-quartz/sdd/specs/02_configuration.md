# Feature Specification: Type-Safe Configuration Properties

| Metadata | Value |
| :--- | :--- |
| **Feature Name** | Type-Safe Configuration Properties |
| **Spec Version** | 1.0.0 |
| **Status** | Approved |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
Provides a centralized, type-safe way to configure the carbon-aware scheduler using Spring's `@ConfigurationProperties` mechanism.

## 2. Included Subfeatures
- **`CarbonAwareProperties` Bean**: A class mapping properties under the `carbon.aware.scheduling` prefix.
- **Global Toggles**: `enabled` (master toggle) and `dry-run` (simulated scheduling).
- **Statistics Toggle**: `statistics.enabled` to control recording of carbon intensity data.
- **Forecast Configuration**: `open-data.enabled` and `open-data.locations` for the built-in provider.
- **IDE Autocomplete Support**: Integration with `spring-boot-configuration-processor` to generate `spring-configuration-metadata.json`.
- **Override Properties**: Optional properties to steer bean selection without code changes.

## 3. Explicitly NOT Included
- **Dynamic Property Updates**: Changing properties at runtime without application restart is NOT in scope.
- **Custom Property Prefixes**: Only `carbon.aware.scheduling` is supported; user-defined prefixes are NOT included.

## 4. Technical Details
- **Prefix**: `carbon.aware.scheduling`
- **Supported Properties**:
    - `carbon.aware.scheduling.enabled` (boolean, default: `true`)
    - `carbon.aware.scheduling.dry-run` (boolean, default: `false`)
    - `carbon.aware.scheduling.statistics.enabled` (boolean, default: `false`)
    - `carbon.aware.scheduling.open-data.enabled` (boolean, default: `false`)
    - `carbon.aware.scheduling.open-data.locations` (List<String>, default: `[]`)
    - `carbon.aware.scheduling.forecast.bean-name` (String, optional) — explicit Spring bean name to use for `CarbonForecastApi`.
    - `carbon.aware.scheduling.forecast.impl-class` (String, optional) — FQCN to use if no Spring bean is desired; bridges to SPI behavior.
    - `carbon.aware.scheduling.persistence.bean-name` (String, optional) — explicit Spring bean name to use for `PersistenceApi`.
    - `carbon.aware.scheduling.persistence.impl-class` (String, optional) — FQCN to use if no Spring bean is desired; bridges to SPI behavior.

## 5. Test Strategy
- Refer to [Test Specification](../test-specification.md): UT-1 (property binding), UT-2 (default values). Metadata generation is validated via the build artifact (`spring-configuration-metadata.json`).
