package com.esentri.quartz.camelexample;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.shared.clients.forecast.TestForecastClient;
import org.apache.camel.builder.RouteBuilder;

import static org.quartz.TriggerBuilder.newTrigger;

public class ScheduledRoute extends RouteBuilder {
  private static final String ROUTE_ID = ScheduledRoute.class.getName();

  @Override
  public void configure() {

    CarbonAwareCronTrigger carbonAwareTrigger = newTrigger()
        .withIdentity("quartzCarbonCamel", "carbon-aware")
        .forJob("quartzCarbonCamel", "carbon-aware")
        .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("38 0/1 * ? * *")
            .withJobDurationInMinutes(7)
            .withDeadlineCronExpression("50 0/1 * ? * *")
            .withLocation("de")
            .withCarbonForecastApi(new TestForecastClient()))
        .build();



    from(CarbonAwareQuartzEndpointBuilder.build(carbonAwareTrigger, getContext()))
        .routeId(ROUTE_ID)
        .log("Run some task in camel route...")
        .end();

  }


}
