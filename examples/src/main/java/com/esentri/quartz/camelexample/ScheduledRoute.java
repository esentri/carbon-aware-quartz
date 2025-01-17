package com.esentri.quartz.camelexample;

import static org.quartz.TriggerBuilder.newTrigger;

import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.forecast.client.TestClient;
import org.apache.camel.builder.RouteBuilder;

public class ScheduledRoute extends RouteBuilder {
  private static final String ROUTE_ID = ScheduledRoute.class.getName();

  @Override
  public void configure() throws Exception {

    CarbonAwareCronTrigger carbonAwareTrigger = newTrigger()
        .withIdentity("quartzCarbonCamel", "carbon-aware")
        .forJob("quartzCarbonCamel", "carbon-aware")
        .withSchedule(CarbonAwareCronScheduleBuilder.cronSchedule("38 0/1 * ? * *")
            .withJobDurationInMinutes(7)
            .withDeadlineCronExpression("50 0/1 * ? * *")
            .withLocation("de")
            .withCarbonForecastApi(new TestClient()))
        .build();



    from(CarbonAwareQuartzEndpointBuilder.build(carbonAwareTrigger, getContext()))
        .routeId(ROUTE_ID)
        .log("Run some task in camel route...")
        .end();

  }


}
