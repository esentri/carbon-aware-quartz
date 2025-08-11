package com.esentri.quartz.camelexample;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.endpoint.dsl.QuartzEndpointBuilderFactory;
import org.apache.camel.component.quartz.QuartzEndpoint;
import org.apache.camel.component.quartz.SchedulerInitTask;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.quartz;

/**
 * A utility builder for creating Camel QuartzEndpoints with Carbon-Aware scheduling capabilities.
 *
 * <p>This builder class provides factory methods for creating Apache Camel QuartzEndpoints
 * that are configured to use Carbon-Aware triggers instead of standard Quartz triggers.
 * The builder handles the complex initialization required to integrate Carbon-Aware
 * scheduling with Camel's Quartz component.</p>
 *
 * <p>The class replaces the default Camel trigger mechanism with Carbon-Aware triggers,
 * enabling jobs to be scheduled based on carbon intensity data and environmental
 * considerations. This allows applications to optimize their execution timing
 * for reduced carbon footprint.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Seamless integration of Carbon-Aware triggers with Camel routes</li>
 *   <li>Automatic replacement of default Camel triggers</li>
 *   <li>Support for standard Quartz scheduler factory with veto plugins</li>
 *   <li>Builder pattern for clean endpoint configuration</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * CarbonAwareCronTrigger trigger = new CarbonAwareCronTrigger("myTrigger", "myGroup", "0 0 * * * ?");
 * CamelContext context = new DefaultCamelContext();
 * QuartzEndpoint endpoint = CarbonAwareQuartzEndpointBuilder.build(trigger, context);
 * }</pre>
 *
 * @author Carbon-Aware-Quartz Framework
 * @version 1.0
 * @since 1.0
 *
 * @see CarbonAwareCronTrigger
 * @see QuartzEndpoint
 * @see SchedulerInitTask
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CarbonAwareQuartzEndpointBuilder {

  /**
   * Builds a QuartzEndpoint configured with the specified Carbon-Aware trigger.
   *
   * <p>This method creates a new QuartzEndpoint using the provided Carbon-Aware trigger
   * and configures it to work with Camel's routing engine. The endpoint is configured
   * with a custom scheduler initialization task that replaces the default Camel trigger
   * with the provided Carbon-Aware trigger.</p>
   *
   * <p>The build process involves:</p>
   * <ol>
   *   <li>Creating a QuartzEndpoint using the trigger's key information</li>
   *   <li>Adding a scheduler initialization task for trigger replacement</li>
   *   <li>Configuring the endpoint with a standard Quartz scheduler factory</li>
   *   <li>Enabling veto plugin support for Carbon-Aware scheduling</li>
   * </ol>
   *
   * <p><strong>Implementation Note:</strong> The method uses the trigger's group
   * and name to construct the endpoint URI in the format "group/name".</p>
   *
   * @param trigger the Carbon-Aware trigger to be used for scheduling.
   *               Must not be {@code null} and must have a valid key
   * @param context the Camel context in which the endpoint will operate.
   *               Must not be {@code null} and should be properly initialized
   *
   * @return a fully configured QuartzEndpoint ready for use in Camel routes
   *
   * @throws NullPointerException if either trigger or context is {@code null}
   * @throws IllegalArgumentException if the trigger key is invalid or malformed
   *
   * @see QuartzEndpoint
   * @see CarbonAwareCronTrigger#getKey()
   * @see StdSchedulerFactory
   */
  public static QuartzEndpoint build(CarbonAwareCronTrigger trigger, CamelContext context) {
    QuartzEndpointBuilderFactory.QuartzEndpointBuilder builder = quartz(trigger.getKey().getGroup() + "/" + trigger.getKey().getName());
    QuartzEndpoint endpoint = (QuartzEndpoint) builder.resolve(context);

    endpoint.getComponent().addScheduleInitTask(replaceDefaultCamelTriggerWithCarbonAwareTrigger(trigger));
    // use standard scheduler with the enabled veto plugin
    endpoint.getComponent().setSchedulerFactory(new StdSchedulerFactory());

    return endpoint;
  }

  /**
   * Creates a scheduler initialization task that replaces the default Camel trigger with a Carbon-Aware trigger.
   *
   * <p>This method returns a {@link SchedulerInitTask} that performs the necessary
   * operations to replace Camel's default trigger mechanism with the provided
   * Carbon-Aware trigger. The task is executed during scheduler initialization
   * and handles the complex job detail retrieval and rescheduling process.</p>
   *
   * <p>The replacement process works by:</p>
   * <ol>
   *   <li>Retrieving the existing JobDetail from the scheduler</li>
   *   <li>Removing the existing job and its default trigger</li>
   *   <li>Rescheduling the job with the Carbon-Aware trigger</li>
   * </ol>
   *
   * <p><strong>Thread Safety:</strong> The returned task is designed to be
   * executed in a controlled scheduler initialization context and should not
   * be called concurrently.</p>
   *
   * <p><strong>Error Handling:</strong> The task assumes that the job exists
   * in the scheduler. If the job is not found, a QuartzException may be thrown
   * during task execution.</p>
   *
   * @param trigger the Carbon-Aware trigger to replace the default trigger with.
   *               Must not be {@code null} and must have a valid job key
   *
   * @return a SchedulerInitTask that performs the trigger replacement operation
   *
   * @throws NullPointerException if the trigger is {@code null}
   *
   * @see SchedulerInitTask
   * @see Trigger#getJobKey()
   * @see org.quartz.Scheduler#deleteJob(org.quartz.JobKey)
   * @see org.quartz.Scheduler#scheduleJob(JobDetail, Trigger)
   */
  static SchedulerInitTask replaceDefaultCamelTriggerWithCarbonAwareTrigger(Trigger trigger) {
    return (scheduler -> {
      JobDetail jobDetail1 = scheduler.getJobDetail(trigger.getJobKey());
      scheduler.deleteJob(trigger.getJobKey());
      scheduler.scheduleJob(jobDetail1, trigger);
    });
  }

}