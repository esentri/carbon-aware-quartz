# Quartz Module: Core Carbon-Aware Implementation

This module contains the core components for the carbon-aware extension of the Quartz Scheduler.

## Key Components

- **Plugins**: 
    - `CarbonAwarePlugin`: The main entry point to enable carbon-aware capabilities in Quartz.
- **Listeners**:
    - `TimeShiftingTriggerListener`: Handles the logic for rescheduling jobs to periods of lower carbon intensity.
    - `CarbonStatisticsTriggerListener`: Collects and records statistics about saved emissions.
- **Triggers**:
    - `CarbonAwareCronTrigger`: A custom cron trigger that supports time-shifting.
- **Clients & SPI**:
    - `CarbonForecastApi`: SPI for integrating external carbon forecast providers.
    - `PersistenceApi`: SPI for saving carbon-aware execution statistics.
    - `OpenDataForecastClient`: Default implementation for fetching carbon data from open APIs (e.g., Energy Charts).

## Module Dependencies

This module extends the standard Quartz Scheduler and requires:
- **Quartz (2.5.0)**: Base scheduling framework.
- **SLF4J (2.0.17)**: Logging abstraction.
- **WireMock (3.13.1)**: Used in tests for mocking external carbon APIs.
- **AssertJ (3.27.2)** & **Mockito (5.15.2)**: For comprehensive unit testing.

## Implementation Details

- **Time-Shifting**: The scheduler analyzes carbon intensity forecasts within a configurable time window to determine the optimal execution time.
- **Service Discovery**: Uses Java's `ServiceLoader` to dynamically discover implementations of `CarbonForecastApi` and `PersistenceApi`.
- **States**: Maintains `CarbonAwareExecutionState` to track the lifecycle of time-shifted triggers.
