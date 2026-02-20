#### Functional

- [ ] Provide `@EnableCarbonAwareScheduling` annotation for Spring Boot applications.
- [ ] Automatically configure `CarbonAwarePlugin` when the annotation is present.
- [ ] Ensure the extension remains dormant if the annotation is missing.
- [ ] Automatically attach `TimeShiftingTriggerListener` to the `Scheduler`.
- [ ] Automatically attach `CarbonStatisticsTriggerListener` to the `Scheduler` when statistics are enabled.
- [ ] Support `carbon.aware.scheduling` property prefix for all configuration options.
- [ ] Provide a global toggle via `carbon.aware.scheduling.enabled`.
- [ ] Enable/disable dry-run mode via `carbon.aware.scheduling.dry-run`.
- [ ] Toggle OpenData forecast provider via `carbon.aware.scheduling.open-data.enabled`.
- [ ] Configure OpenData locations via `carbon.aware.scheduling.open-data.locations`.
- [ ] Auto-detect and use custom `CarbonForecastApi` beans from the Spring context.
- [ ] Auto-detect and use custom `PersistenceApi` beans from the Spring context.
- [ ] Allow overriding forecast and persistence implementations via `@Primary` or configuration.

#### Technical

- [ ] Generate Spring Boot configuration metadata for IDE autocomplete support.
- [ ] Ensure compatibility with Java 17 and Gradle 8.11.
- [ ] All public classes, methods, and attributes must have Javadoc.
- [ ] Provide comprehensive unit tests for auto-configuration and property mapping.
- [ ] Include an `AGENTS.md` file within the extension module explaining its usage.
- [ ] Ensure the extension does not conflict with existing Quartz `SchedulerCustomizer` beans.