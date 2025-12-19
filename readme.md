# Carbon-Aware Quartz Scheduler

This project is an extension of
the [quartz-scheduler](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc) project.
All functions of the Quartz-Scheduler are still supported.
The project is enhanced by carbon-aware principles from
the [Green Software Foundation](https://greensoftware.foundation/)
All principles of the Green Software Foundation can be found [here](https://learn.greensoftware.foundation/)

## License

This project is primarily licensed under the **MIT License**.  
However, some files include code from the [Quartz Scheduler](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc) project,  
which is licensed under the **Apache License 2.0**.

For details, see the [LICENSE](LICENSE) file.
[quartz.properties](quartz/src/main/resources/quartz.properties)

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

As this project is an enhancement of the quartz-scheduler, the quartz dependency is required on the projects classpath.
Also, an implementation of Slf4J is required on the projects classpath.
```groovy
dependencies {
    implementation "org.quartz-scheduler:quartz:2.5.X"
    implementation "org.slf4j:slf4j-api:2.X.X"
    implementation 'com.esentri:quartz:1.1.0'
}
```

Now you can configure your Scheduler and Jobs. By default, the carbon-aware plugin is inactive and must be configured via
application properties.

#### User Guide

Quartz allows you by default to register some plugins to enhance its default functionality.
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
In the Example below the Trigger will fire at 10:00pm every day, and have to be finished at 4:00am on the next day.

When the trigger fires at 10:00pm, it fetches the carbon Forecast between 10:00pm and 04:00am. If there is a period of
time when the energy is greener (let's say 02:35am), the trigger will fire again at 02:35am to execute the Job.
The actual execution (10:00pm) is canceled.

The example below uses the default open-date API client, fetching data from the
[Energy-Charts API](`https://api.energy-charts.info/`).

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
                .useDefaultOpenDataForcastApiClient())
        .build();
```

This example uses a custom implementation for forcasting.

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
This is a minimal Subset of Carbon Aware SDK, which is linked above.

For simplicity use the
`[OpenDataForecastClient.java](quartz/src/main/java/com/esentri/quartz/carbonaware/clients/opendata/OpenDataForecastClient.java)`.
It will fetch and cache the forecast for the locations configured properties in `quartz.properties`. This reduces the
rest calls made during the time-shifting operations.

##### Statistics

To track the saved emissions, a statistics plugin can be enabled via application properties

```properties
org.quartz.plugin.<NAME>.enableStatistics=true
org.quartz.plugin.<NAME>.restClientImplementationClass=<implementation of com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi.cass>
org.quartz.plugin.<NAME>.persistenceClientImplementationClass=<implementation of com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi.cass>
```

The `restClientImplementationClass` can be the same implementation of `CarbonForecastApi.class` as passed into the
Trigger.
This is required to fetch the Carbon Intensity for the initial timestamp and the re-scheduled timestamp.
To store this information, a `persistenceClientImplementationClass` is required, which implements the `PersistenceApi.class` interface.

##### SPI-based client discovery (ServiceLoader)

From version 1.1.0, Carbon-Aware Quartz no longer uses reflection (Class.forName/newInstance) to construct client implementations. Instead, it relies entirely on the Java Service Provider Interface (SPI) via ServiceLoader. This works well in standard JVMs and in Quarkus (including native-image) when providers are properly registered.

- What is discovered via SPI?
  - com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi
  - com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi

- How to register your implementation
  1) Implement one of the interfaces above.
  2) Add a file under META-INF/services with the fully qualified name of the interface.
  3) Put the fully qualified name(s) of your implementation class(es) as lines in that file.

  Example for a custom CarbonForecastApi implementation:
  - File: META-INF/services/com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi
    Content:
    com.example.quartz.clients.rest.MyForecastClient

  Example for a custom PersistenceApi implementation:
  - File: META-INF/services/com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi
    Content:
    com.example.quartz.clients.persistence.MyPersistenceClient

- Selecting a specific provider via properties (optional)
  - org.quartz.plugin.<NAME>.restClientImplementationClass: Either the fully qualified name or the simple class name of one of the registered CarbonForecastApi providers.
  - org.quartz.plugin.<NAME>.persistenceClientImplementationClass: Either the fully qualified name or the simple class name of one of the registered PersistenceApi providers.
  - If these properties are omitted and exactly one provider for an API is present on the classpath, that single provider is selected automatically.

- Quarkus/native-image note
  - Ensure your META-INF/services resources are included in the final application (and the native image if you build one). In Quarkus, providers discovered via ServiceLoader are supported; just make sure the provider classes and META-INF/services files are part of your application or its dependencies.

#### Examples

1. [Simple Time-Shifted job execution](./examples/src/main/java/com/esentri/quartz/example1/readme.md)
2. [Dry-run enabled](./examples/src/main/java/com/esentri/quartz/example2/readme.md)
3. [Statistics enabled](./examples/src/main/java/com/esentri/quartz/example3/readme.md)
4. [Usage of default open-data restclient for forcasting](./examples/src/main/java/com/esentri/quartz/example4/readme.md)
5. [Apache Camel](./examples/src/main/java/com/esentri/quartz/camelexample/readme.md)
6. [SpringBoot](./examples/src/main/java/com/esentri/quartz/springboot/readme.md)

#### Properties

| property                                                        | type      | default | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|-----------------------------------------------------------------|-----------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `org.quartz.plugin.<NAME>.dryrun`                               | `boolean` | `false` | Enables the dryrun feature. The CarbonAwareCronTrigger will determine a better execution time, but the Job will **not** be re-scheduled. All statistics feature will also work in combination with this feature.                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `org.quartz.plugin.<NAME>.enableStatistics`                     | `boolean` | `false` | Enables the statisctis feature. To persist the information about the saved carbon intensity.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `org.quartz.plugin.<NAME>.restClientImplementationClass`        | `Class`   | `null`  | The implementation class for the `CarbonForecastApi.class` used in statistics feature. Only required if, `enableStatistics=true`. Implementation Class have to provide a default constructor, for instantiation. For simplicity the `[OpenDataForecastClient.java](quartz/src/main/java/com/esentri/quartz/carbonaware/clients/opendata/OpenDataForecastClient.java)` class can be used. Therefore the `useOpenDataProvider` property has to be activated.                                                                                                                                                                                                                           |
| `org.quartz.plugin.<NAME>.persistenceClientImplementationClass` | `Class`   | `null`  | The implementation class for the `PersistenceApi.class` used in statistics feature. Only required if, `enableStatistics=true`. Implementation Class have to provide a default constructor, for instantiation.                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                      |
| `org.quartz.plugin.<NAME>.useOpenDataProvider`                  | `boolean` | `true`  | Enables forecasting with Open-Data from the [Energy-Charts API](`https://api.energy-charts.info/`). If this is set to `true` a list of `openDataLocations` have to be provided. The data fetched from the api will be stored in a cache and will be automatically updated. Caching this data reduces the overall api calls and thus also the Carbon-Intensity of the application. Forecasts for the next day usually available round about 7pm. The period will then reach until the next day at 10pm. The update schedule for this data can be found in class [OpenDataUpdateJob.java](quartz/src/main/java/com/esentri/quartz/carbonaware/clients/opendata/OpenDataUpdateJob.java) |
| `org.quartz.plugin.<NAME>.openDataLocations`                    | `String`  | `de`    | A string separated by commas like `de,at,ch`. This will fetch and cache the forecast for this 3 locations if the `useOpenDataProvider` property is set to true. A possible list of supported locations can be found in class [Location.java](quartz/src/main/java/com/esentri/quartz/carbonaware/clients/opendata/model/Location.java).                                                                                                                                                                                                                                                                                                                                              |                                                                                                                                                                                                      |

