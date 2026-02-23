# Changelog

All notable changes to the Spring Boot Carbon-Aware Quartz starter will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-02-23

### Added

#### Core Features
- **Annotation-based Activation**: New `@EnableCarbonAwareScheduling` annotation for simple, declarative activation of carbon-aware scheduling in Spring Boot applications
- **Type-Safe Configuration**: Complete configuration support under `carbon.aware.scheduling.*` prefix with IDE autocomplete support via Spring Boot configuration processor
- **Automatic Quartz Integration**: Seamless integration with Spring Boot's Quartz auto-configuration through `SchedulerFactoryBeanCustomizer`
- **Spring Bean Auto-Detection**: Automatic discovery and usage of `CarbonForecastApi` and `PersistenceApi` Spring beans with smart resolution strategy

#### Configuration Properties
- `carbon.aware.scheduling.enabled` - Master toggle to enable/disable carbon-aware scheduling (default: `true`)
- `carbon.aware.scheduling.dry-run` - Enable dry-run mode for testing without actual rescheduling (default: `false`)
- `carbon.aware.scheduling.statistics.enabled` - Enable carbon intensity statistics collection (default: `false`)
- `carbon.aware.scheduling.open-data.enabled` - Enable built-in OpenData (EnergyCharts) forecast provider (default: `false`)
- `carbon.aware.scheduling.open-data.locations` - Configure locations for OpenData provider (e.g., `de`, `fr`)
- `carbon.aware.scheduling.forecast.bean-name` - Explicitly specify forecast provider bean name
- `carbon.aware.scheduling.forecast.impl-class` - Specify forecast provider implementation class
- `carbon.aware.scheduling.persistence.bean-name` - Explicitly specify persistence provider bean name
- `carbon.aware.scheduling.persistence.impl-class` - Specify persistence provider implementation class

#### Smart Dependency Resolution
- **Bean Selection Precedence**: Automatic resolution with the following priority:
  1. Property-specified bean name (`bean-name`)
  2. `@Primary` annotated bean
  3. Single auto-detected bean by type
  4. Property-specified implementation class (`impl-class`)
  5. SPI fallback for backward compatibility
- **Fail-Fast Behavior**: Clear error messages when multiple beans exist without explicit selection
- **Spring-to-Quartz Bridging**: Transparent bridge between Spring ApplicationContext and Quartz SPI system

#### Integration Components
- `CarbonAwareSchedulingAutoConfiguration` - Main auto-configuration class
- `CarbonAwareProperties` - Type-safe configuration properties holder
- `CarbonAwareSchedulerCustomizer` - Quartz scheduler customizer for plugin registration
- `ApiProviderResolver` - Smart bean detection and resolution logic
- `SpringBeanRegistry` - Bridge for Spring beans to Quartz SPI system
- `SpringBeanCarbonForecastApiWrapper` - SPI wrapper for forecast API beans
- `SpringBeanPersistenceApiWrapper` - SPI wrapper for persistence API beans

#### Developer Experience
- **IDE Autocomplete**: Full support for configuration properties in IntelliJ IDEA, Eclipse, and VS Code
- **Comprehensive Javadoc**: Complete API documentation for all public classes and methods
- **Clear Error Messages**: Descriptive exceptions when configuration is ambiguous or incorrect
- **Conditional Loading**: Extension remains dormant when annotation is not present or when explicitly disabled

#### Testing Support
- Unit tests for property binding and default values
- Integration tests for activation scenarios (positive and negative cases)
- Integration tests for listener attachment and configuration
- Integration tests for bean resolution precedence
- Integration tests for OpenData provider initialization

### Changed
- Simplified configuration from complex `spring.quartz.properties.org.quartz.plugin.*` format to clean `carbon.aware.scheduling.*` structure
- Improved dependency injection patterns - removed need for `ApplicationContextProvider` utility
- Enhanced error handling with fail-fast validation during application startup

### Technical Details
- **Minimum Requirements**: Java 17, Spring Boot 3.4.1+, Quartz 2.5.0
- **Dependencies**: `spring-boot-starter-quartz`, `spring-boot-autoconfigure`, `spring-boot-configuration-processor`
- **Build Tool**: Gradle 8.11
- **Testing**: JUnit 5 with Spring Boot Test framework

### Documentation
- Complete usage guide in `AGENTS.md`
- Detailed architecture documentation in `sdd/architecture.md`
- Technical specifications in `sdd/specs/*.md`
- Test specification in `sdd/test-specification.md`
- Acceptance criteria in `sdd/acceptence-criteria.md`

### Migration Guide
For users migrating from direct Quartz configuration to the Spring Boot starter:

**Before (Manual Configuration):**
```properties
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.class=com.esentri.quartz.carbonaware.plugins.CarbonAwarePlugin
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.dryrun=false
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.enableStatistics=true
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.useOpenDataProvider=true
spring.quartz.properties.org.quartz.plugin.carbon-aware-plugin.openDataLocations=de
```

**After (Starter Configuration):**
```properties
carbon.aware.scheduling.enabled=true
carbon.aware.scheduling.dry-run=false
carbon.aware.scheduling.statistics.enabled=true
carbon.aware.scheduling.open-data.enabled=true
carbon.aware.scheduling.open-data.locations=de
```

**Code Changes:**
```java
// Add annotation to main application class
@SpringBootApplication
@EnableCarbonAwareScheduling
public class MyApplication {
    // ...
}

// Remove SPI service files from META-INF/services/
// Spring beans are now auto-detected
```

### Development Methodology

This module was developed using **AI-assisted Spec-Driven Development (SDD)**:
- ✅ All features were specified in detailed technical specifications before implementation
- ✅ AI generated code following the specifications with human oversight
- ✅ Complete traceability from requirements → specs → implementation → tests
- ✅ Comprehensive testing and documentation generated alongside code
- ✅ Every component can be traced back to its specification

**Development Workflow:**
1. Write detailed specifications in `sdd/specs/*.md`
2. Define acceptance criteria in `sdd/acceptence-criteria.md`
3. Document architecture in `sdd/architecture.md`
4. Generate task board combining all artifacts
5. AI implements features following task board
6. AI updates CHANGELOG and README

See the [README.md Development Approach section](README.md#development-approach-ai-powered-spec-driven-development) for complete details on extending this module using the same methodology.

## [Unreleased]

### Planned
- Additional forecast provider integrations
- Metrics and monitoring support via Spring Boot Actuator
- Advanced scheduling strategies
- Cloud-native deployment guides

---

For more information, see the [README.md](README.md) and [AGENTS.md](AGENTS.md) files.
