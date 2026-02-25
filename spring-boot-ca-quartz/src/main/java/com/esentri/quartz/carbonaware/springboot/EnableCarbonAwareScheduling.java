package com.esentri.quartz.carbonaware.springboot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables carbon-aware scheduling for Quartz jobs in the application.
 * <p>
 * This annotation must be placed on a {@code @Configuration} or {@code @SpringBootApplication}
 * class to activate the carbon-aware scheduling extension.
 * <p>
 * The extension can be disabled by setting the property {@code carbon.aware.scheduling.enabled}
 * to {@code false}.
 *
 * @see CarbonAwareSchedulingAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CarbonAwareSchedulingAutoConfiguration.class)
public @interface EnableCarbonAwareScheduling {
}
