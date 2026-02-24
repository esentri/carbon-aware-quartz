# Spring Boot Carbon-Aware Quartz Starter

A Spring Boot starter that brings carbon-aware scheduling capabilities to your Quartz-based applications with minimal configuration.

## Overview

The `spring-boot-ca-quartz` starter simplifies the integration of carbon-aware scheduling into Spring Boot applications by providing:

- ✅ **Simple Activation**: Single `@EnableCarbonAwareScheduling` annotation
- ✅ **Type-Safe Configuration**: IDE autocomplete for all settings
- ✅ **Smart Auto-Detection**: Automatic discovery of custom forecast and persistence providers
- ✅ **Zero Boilerplate**: No manual Quartz plugin configuration needed
- ✅ **Production Ready**: Comprehensive testing and error handling

## Quick Start

### 1. Add Dependency

**Gradle:**
```gradle
dependencies {
    implementation 'com.esentri:spring-boot-ca-quartz:1.2.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.esentri</groupId>
    <artifactId>spring-boot-ca-quartz</artifactId>
    <version>1.2.0</version>
</dependency>
```

### 2. Enable Carbon-Aware Scheduling

Add the annotation to your main application class:

```java
@SpringBootApplication
@EnableCarbonAwareScheduling
public class MyApplication {
    static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. Configure

Add configuration to your `application.yml`:

```yaml
carbon:
  aware:
    scheduling:
      enabled: true
      dry-run: false
      statistics:
        enabled: true
      open-data:
        enabled: true
        locations:
          - de
          - fr
```

Or use `application.properties`:

```properties
carbon.aware.scheduling.enabled=true
carbon.aware.scheduling.dry-run=false
carbon.aware.scheduling.statistics.enabled=true
carbon.aware.scheduling.open-data.enabled=true
carbon.aware.scheduling.open-data.locations=de,fr
```

That's it! Your Quartz jobs will now be scheduled considering carbon intensity forecasts.

## Configuration Reference

### Master Toggle

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.enabled` | Boolean | `true` | Master switch to enable/disable carbon-aware scheduling |

### Dry-Run Mode

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.dry-run` | Boolean | `false` | When enabled, simulates time-shifting without actual rescheduling |

### Statistics Collection

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.statistics.enabled` | Boolean | `false` | Enable recording of carbon intensity statistics |

### OpenData Provider

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.open-data.enabled` | Boolean | `false` | Enable built-in EnergyCharts forecast provider |
| `carbon.aware.scheduling.open-data.locations` | List<String> | `[]` | Location codes for forecast data (e.g., `de`, `fr`, `ch`) |

### Advanced Configuration

#### Custom Forecast Provider

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.forecast.bean-name` | String | `null` | Explicitly specify which forecast bean to use |
| `carbon.aware.scheduling.forecast.impl-class` | String | `null` | Fully-qualified class name for non-Spring forecast provider |

#### Custom Persistence Provider

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `carbon.aware.scheduling.persistence.bean-name` | String | `null` | Explicitly specify which persistence bean to use |
| `carbon.aware.scheduling.persistence.impl-class` | String | `null` | Fully-qualified class name for non-Spring persistence provider |

## Usage Examples

### Example 1: Using OpenData Provider

Perfect for getting started quickly with real carbon intensity data:

```yaml
carbon:
  aware:
    scheduling:
      enabled: true
      open-data:
        enabled: true
        locations:
          - de
```

### Example 2: Custom Forecast Provider

Implement your own forecast source:

```java
@Component
public class CustomForecastProvider implements CarbonForecastApi {

    @Override
    public List<EmissionForecast> getEmissionForecastCurrent(
            List<String> location,
            LocalDateTime dataStartAt,
            LocalDateTime dataEndAt,
            Integer windowSize) {
        // Your custom implementation
        return fetchFromYourApi(location, dataStartAt, dataEndAt, windowSize);
    }
}
```

The starter will automatically detect and use your custom provider. No additional configuration needed!

### Example 3: Multiple Providers with @Primary

When you have multiple providers, use `@Primary` to specify which one to use:

```java
@Primary
@Component
public class ProductionForecastProvider implements CarbonForecastApi {
    // This will be used
}

@Component
public class TestForecastProvider implements CarbonForecastApi {
    // This will be ignored
}
```

Or specify explicitly in properties:

```yaml
carbon:
  aware:
    scheduling:
      forecast:
        bean-name: productionForecastProvider
```

### Example 4: Statistics Persistence

Store carbon intensity statistics in your database:

```java
@Component
public class DatabasePersistenceProvider implements PersistenceApi {

    private final CarbonStatisticsRepository repository;

    public DatabasePersistenceProvider(CarbonStatisticsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void persist(CarbonStatisticDto dto) {
        repository.save(mapToEntity(dto));
    }
}
```

Enable statistics collection:

```yaml
carbon:
  aware:
    scheduling:
      statistics:
        enabled: true
```

### Example 5: Dry-Run Testing

Test your carbon-aware configuration without actually rescheduling jobs:

```yaml
carbon:
  aware:
    scheduling:
      dry-run: true
      statistics:
        enabled: true
```

This will log what would have been rescheduled, allowing you to verify behavior before production deployment.

## How It Works

### Bean Resolution Strategy

The starter uses intelligent bean resolution with the following precedence:

1. **Property-specified bean name**: `carbon.aware.scheduling.forecast.bean-name`
2. **@Primary annotated bean**: Marked with Spring's `@Primary` annotation
3. **Single auto-detected bean**: When only one bean of the type exists
4. **Property-specified implementation class**: `carbon.aware.scheduling.forecast.impl-class`
5. **SPI fallback**: Standard Java ServiceLoader mechanism

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Spring Boot Application                                     │
│                                                             │
│  @SpringBootApplication                                     │
│  @EnableCarbonAwareScheduling  ◄─── Activation Trigger      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ CarbonAwareSchedulingAutoConfiguration                      │
│                                                             │
│  • Loads CarbonAwareProperties                              │
│  • Creates ApiProviderResolver                              │
│  • Registers CarbonAwareSchedulerCustomizer                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Quartz Scheduler (via SchedulerFactoryBeanCustomizer)       │
│                                                             │
│  • CarbonAwarePlugin registered                             │
│  • TimeShiftingTriggerListener attached                     │
│  • CarbonStatisticsTriggerListener (conditional)            │
└─────────────────────────────────────────────────────────────┘
```

### Time-Shifting Process

1. **Job Trigger**: Quartz is about to fire a job at the configured time
2. **Forecast Query**: TimeShiftingTriggerListener queries the forecast provider
3. **Carbon Analysis**: Analyzes carbon intensity within the allowed time window
4. **Optimal Time Selection**: Finds the time slot with lowest carbon intensity
5. **Rescheduling**: Job is rescheduled to the optimal time (unless in dry-run mode)
6. **Statistics Recording**: If enabled, records the carbon intensity data

## Troubleshooting

### Issue: "Multiple beans of type CarbonForecastApi found"

**Solution**: Mark one bean with `@Primary` or specify explicitly:

```yaml
carbon:
  aware:
    scheduling:
      forecast:
        bean-name: myPreferredProvider
```

### Issue: "No CarbonForecastApi bean found"

**Solution**: Either enable OpenData provider or create your own:

```yaml
carbon:
  aware:
    scheduling:
      open-data:
        enabled: true
        locations:
          - de
```

Or provide a custom implementation as a Spring bean.

### Issue: Extension not activating

**Checklist**:
1. ✅ Is `@EnableCarbonAwareScheduling` present on a `@Configuration` class?
2. ✅ Is `carbon.aware.scheduling.enabled` set to `true` (or not set, as `true` is default)?
3. ✅ Is `spring-boot-starter-quartz` in your dependencies?
4. ✅ Check application logs for carbon-aware initialization messages

### Debug Mode

Enable debug logging to see detailed activation information:

```yaml
logging:
  level:
    com.esentri.quartz.carbonaware: DEBUG
```

## Requirements

- **Java**: 17 or higher
- **Spring Boot**: 3.4.1 or higher
- **Quartz**: 2.5.0 or higher

## Dependencies

The starter automatically brings in:
- `spring-boot-starter-quartz`
- `quartz` (carbon-aware core library)

## Testing Your Integration

### Unit Testing

Test your configuration setup:

```java
@SpringBootTest
@EnableCarbonAwareScheduling
class CarbonAwareConfigTest {

    @Autowired
    private CarbonAwareProperties properties;

    @Test
    void shouldLoadConfiguration() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getOpenData().isEnabled()).isTrue();
    }
}
```

### Integration Testing

Verify the complete integration:

```java
@SpringBootTest
@EnableCarbonAwareScheduling
class QuartzIntegrationTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    void shouldHaveTimeShiftingListener() throws SchedulerException {
        List<TriggerListener> listeners =
            scheduler.getListenerManager().getTriggerListeners();

        assertThat(listeners)
            .anyMatch(l -> l.getName().contains("TimeShifting"));
    }
}
```

## Best Practices

### 1. Start with Dry-Run Mode

Always test your configuration with `dry-run: true` first:

```yaml
carbon:
  aware:
    scheduling:
      dry-run: true
```

### 2. Enable Statistics in Production

Track your carbon savings:

```yaml
carbon:
  aware:
    scheduling:
      statistics:
        enabled: true
```

### 3. Use Property-based Configuration

Prefer property files over hardcoded values for easier environment-specific configuration.

### 4. Monitor Your Forecast Provider

Implement health checks for your custom forecast providers to ensure reliable carbon-aware scheduling.

### 5. Configure Appropriate Time Windows

Set realistic deadlines for your jobs to give the scheduler flexibility in finding optimal time slots.

## Migration from Manual Configuration

If you're currently using manual Quartz plugin configuration:

### Step 1: Remove Manual Configuration

**Remove from application.properties:**
```properties
# Remove these lines
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.class=...
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.dryrun=...
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.enableStatistics=...
```

### Step 2: Add Annotation

```java
@SpringBootApplication
@EnableCarbonAwareScheduling  // Add this
public class MyApplication {
    // ...
}
```

### Step 3: Update Configuration

```yaml
carbon:
  aware:
    scheduling:
      enabled: true
      dry-run: false
      statistics:
        enabled: true
```

### Step 4: Remove SPI Service Files

If you had custom providers registered via SPI, remove files from:
- `META-INF/services/com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi`
- `META-INF/services/com.esentri.quartz.carbonaware.clients.persistence.PersistenceApi`

Your Spring beans will be auto-detected!

### Step 5: Simplify Bean Creation

**Before:**
```java
public class MyProvider implements CarbonForecastApi {
    public MyProvider() {
        // No-arg constructor for SPI
        ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        this.dependency = ctx.getBean(MyDependency.class);
    }
}
```

**After:**
```java
@Component
public class MyProvider implements CarbonForecastApi {
    private final MyDependency dependency;

    public MyProvider(MyDependency dependency) {
        // Clean dependency injection!
        this.dependency = dependency;
    }
}
```

## Development Approach

This module was developed using **AI-assisted Spec-Driven Development (SDD)**, where specifications are written first and AI generates implementation following those specs with human oversight.

**Key Benefits:**
- ✅ Complete traceability from requirements to implementation
- ✅ Comprehensive testing and documentation from day one
- ✅ Consistent quality across all features
- ✅ Accelerated development without sacrificing maintainability

**For complete details on the SDD methodology, extending this module, and the AI-assisted workflow, see:**

📖 **[SDD Documentation](sdd/README.md)** - Complete guide to Spec-Driven Development with AI

All development artifacts (specifications, architecture, task boards) are in the [`sdd/`](./sdd) directory.

## Contributing

**When contributing new features, please follow the Spec-Driven Development workflow documented in [`sdd/README.md`](sdd/README.md) to ensure consistency and quality.**

## License

See the [LICENSE](../LICENSE) file for details.

## Support

- **Documentation**: See [AGENTS.md](AGENTS.md) for detailed development guidelines
- **Issues**: Report bugs or request features on the project issue tracker
- **Examples**: Check the `examples/` directory for working implementations

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and migration guides.

## Related Resources

- [Quartz Scheduler](http://www.quartz-scheduler.org/)
- [Spring Boot Quartz Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.quartz)
- [Carbon-Aware Computing](https://www.carbon-aware-computing.com/)
- [EnergyCharts API](https://api.energy-charts.info/)

---

**Made with 💚 for a sustainable digital future**
