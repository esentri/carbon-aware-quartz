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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CarbonAwareQuartzEndpointBuilder {

  public static QuartzEndpoint build(CarbonAwareCronTrigger trigger, CamelContext context) {
    QuartzEndpointBuilderFactory.QuartzEndpointBuilder builder = quartz(trigger.getKey().getGroup() + "/" + trigger.getKey().getName());
    QuartzEndpoint endpoint = (QuartzEndpoint) builder.resolve(context);

    endpoint.getComponent().addScheduleInitTask(replaceDefaultCamelTriggerWithCarbonAwareTrigger(trigger));
    // use standard scheduler with the enabled veto plugin
    endpoint.getComponent().setSchedulerFactory(new StdSchedulerFactory());

    return endpoint;
  }

  static SchedulerInitTask replaceDefaultCamelTriggerWithCarbonAwareTrigger(Trigger trigger) {
    return (scheduler -> {
      JobDetail jobDetail1 = scheduler.getJobDetail(trigger.getJobKey());
      scheduler.deleteJob(trigger.getJobKey());
      scheduler.scheduleJob(jobDetail1, trigger);
    });
  }

}
