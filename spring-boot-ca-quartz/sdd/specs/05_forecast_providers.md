# Feature Specification: Integrated Forecast Providers

| Metadata | Value |
| :--- | :--- |
| **Feature Name** | Integrated Forecast Providers |
| **Spec Version** | 1.0.0 |
| **Status** | Approved |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
The extension provides built-in support for carbon forecast providers, starting with the OpenData provider (`EnergyChartsForecastProvider`). This allows users to get started without implementing their own forecast client.

## 2. Included Subfeatures
- **Turnkey OpenData Experience**:
    - Automatic initialization of `EnergyChartsForecastProvider` with configured locations.
    - Automatic scheduling of `OpenDataUpdateJob` to keep the cache fresh.
- **Conditional Auto-Config**:
    - Only initializes OpenData if `carbon.aware.scheduling.open-data.enabled=true`.
    - Yields to custom `CarbonForecastApi` beans found in the context (see Step 4).

## 3. Explicitly NOT Included
- **Third-Party API Integrations**: Other providers requiring API keys (e.g., ElectricityMaps) are NOT included in the core Spring Boot extension but can be added via the SPI.
- **Custom Update Intervals**: Currently, the update interval for OpenData is fixed in the core library's `OpenDataUpdateJob`. Configuring this interval via Spring properties is NOT in scope for this version.

## 4. Technical Details
- **Default Location**: If no locations are provided, it may default to "de".
- **Internal Client**: Uses `EnergyChartsForecastProvider` from the `quartz` core module.

## 5. Test Strategy
- Refer to [Test Specification](../test-specification.md): IT-6 (OpenData turnkey integration).
