# Carbon-Aware Quartz Scheduler

This project is an extension of
the [quartz-scheduler](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc) project.
All functions of the Quartz-Scheduler are still supported.
The project is enhanced by carbon-aware principles from
the [Green Software Foundation](https://greensoftware.foundation/)
All principles of the Green Software Foundation can be found [here](https://learn.greensoftware.foundation/)

## Carbon Awareness

> **[Principle](https://learn.greensoftware.foundation/carbon-awareness)**:
> The core message is, do more when the electricity is cleaner and do less when the electricity is dirtier!

This project supports you, to make the Carbon Footprint of your executed Application a bit greener.

## Quartz Documentation

### Installation

Gradle

```groovy
dependencies {
    implementation "org.quartz-scheduler:quartz:2.5.0"
}
```

Further explanation and a quick-start guide can be
found [here](https://github.com/quartz-scheduler/quartz/blob/main/docs/quick-start-guide.adoc)

## Carbon Aware Quartz

This plugin for the Quartz scheduler allows you to Time-Shift your Scheduled jobs, and Track the Grid Carbon Intensity
at the execution timestamp.

### Installation

As this project is a enhancement of the quartz-scheduler, the quartz dependency is required on projects classpath.

```groovy
dependencies {
    implementation "org.quartz-scheduler:quartz:2.5.0"
    implementation 'com.esentri.quartz:carbon-aware-quartz:1.0.0'
}
```

Now you can configure your Scheduler and Jobs. By default the carbon-aware plugin is inactive and must be configured via
application properties.

#### User Guide

Quartz allows you by default register some plugins to enhance its default functionality.
A detailed Documentation about Plugins can be
found [here](https://github.com/quartz-scheduler/quartz/blob/main/docs/configuration.adoc#configuration-of-plug-ins-add-functionality-to-your-scheduler)

The Carbon-Aware Plugin has to be registered like any other Quartz Plugin.

```properties
org.quartz.plugin.<NAME>.class:com.esentri.quartz.carbonaware.plugins.CarbonAwarePlugin
```

The Quartz Scheduler needs two core components to Work. A Job to execute and a Trigger, which determines the execution
Time.
To enable Time-Shifting, a instance of `CarbonAwareCronTrigger` is required.

The Trigger needs two "timestaps" (Cron-Patterns) and a Client, which delivers a Carbon-Intensity forecast.
In the Example below the Trigger will fire at 10:00pm every day, and have to be finished at 4:00am at the next day.

When the trigger fires at 10:00pm it fetches the carbon Forecast between 10:00pm and 04:00am. If there is a period of
time,
where the energy is greener (lets say 02:35am), the trigger will fire again at 02:35am to execute the Job.
The actual execution (10:00pm) is canceled.

```java
JobDetail job = newJob(TimeShiftedJob.class)
        .withIdentity("TimeShiftedJob", "carbon-aware")
        .ofType(TimeShiftedJob.class)
        .build();

CarbonAwareCronTrigger carbonAwareTrigger = newTrigger()
        .withIdentity("CarbonAwareTrigger", "carbon-aware")
        .forJob("TimeShiftedJob", "carbon-aware")
        .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("0 0 22 ? * *")
                .withJobDurationInMinutes(7)
                .withDeadlineCronExpression("0 0 4 ? * *")
                .withLocation("de")
                .withCarbonForecastApi(new CarbonForecastApi()))
        .build();
```

##### CarbonForecastApi

To get the Carbon forecast the Interface `CarbonForecastApi` have to be implemented.
This is a minimal Subset of Carbon Aware SDK, which is linked above

##### Statistics

To track the saved emissions, a statistics plugin can be enabled via application properties

```properties
org.quartz.plugin.<NAME>.enableStatistics=true
org.quartz.plugin.<NAME>.restClientImplementationClass=<implementation of com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi.cass>
org.quartz.plugin.<NAME>.persistenceClientImplementationClass=<implementation of com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi.cass>
```

The `restClientImplementationClass` can be the same implementation of `CarbonForecastApi.class` as passed into the Trigger. 
This is required to fetch the Carbon Intensity for initial timestamp and the re-scheduled timestamp.
To store this information, a `persistenceClientImplementationClass` is required, which implements the `PersistenceApi.class` interface.

#### Examples

1. [Simple Time-Shifted job execution](./examples/src/main/java/com/esentri/quartz/example1/readme.md)
2. [dry-run enabled](./examples/src/main/java/com/esentri/quartz/example2/readme.md)
3. [statistics enabled](./examples/src/main/java/com/esentri/quartz/example2/readme.md)
4. [Apache Camel](./examples/src/main/java/com/esentri/quartz/camelExample/readme.md)
5. [SpringBoot](./examples/src/main/java/com/esentri/quartz/springboot/readme.md)

#### Properties

| property                                                        | type      | default | description                                                                                                                                                                                                      |
|-----------------------------------------------------------------|-----------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `org.quartz.plugin.<NAME>.dryrun`                               | `boolean` | `false` | Enables the dryrun feature. The CarbonAwareCronTrigger will determine a better execution time, but the Job will **not** be re-scheduled. All statistics feature will also work in combination with this feature. |
| `org.quartz.plugin.<NAME>.enableStatistics`                     | `boolean` | `false` | Enables the statisctis feature. To persist the information about the saved carbon intensity.                                                                                                                     |
| `org.quartz.plugin.<NAME>.restClientImplementationClass`        | `Class`   | `null`  | The implementation class for the `CarbonForecastApi.class` used in statistics feature. Only required, `enableStatistics=true`. Implementation Class have to provide a default constructor, for instantiation.    |
| `org.quartz.plugin.<NAME>.persistenceClientImplementationClass` | `Class`   | `null`  | The implementation class for the `PersistenceApi.class` used in statistics feature. Only required, `enableStatistics=true`. Implementation Class have to provide a default constructor, for instantiation.       |                                                                                                                                                                                                      |
