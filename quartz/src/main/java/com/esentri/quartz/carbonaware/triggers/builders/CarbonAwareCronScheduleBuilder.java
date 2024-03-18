/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package com.esentri.quartz.carbonaware.triggers.builders;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.impl.CarbonAwareCronTriggerImpl;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.MutableTrigger;

import java.text.ParseException;
import java.util.TimeZone;

/**
 * <code>CronScheduleBuilder</code> is a {@link ScheduleBuilder} that defines
 * {@link CronExpression}-based schedules for <code>Trigger</code>s.
 *
 * <p>
 * Quartz provides a builder-style API for constructing scheduling-related
 * entities via a Domain-Specific Language (DSL). The DSL can best be utilized
 * through the usage of static imports of the methods on the classes
 * <code>TriggerBuilder</code>, <code>JobBuilder</code>,
 * <code>DateBuilder</code>, <code>JobKey</code>, <code>TriggerKey</code> and
 * the various <code>ScheduleBuilder</code> implementations.
 * </p>
 *
 * <p>
 * Client code can then use the DSL to write code such as this:
 * </p>
 *
 * <pre>
 * JobDetail job = newJob(MyJob.class).withIdentity(&quot;myJob&quot;).build();
 *
 * Trigger trigger = newTrigger()
 *         .withIdentity(triggerKey(&quot;myTrigger&quot;, &quot;myTriggerGroup&quot;))
 *         .withSchedule(dailyAtHourAndMinute(10, 0))
 *         .startAt(futureDate(10, MINUTES)).build();
 *
 * scheduler.scheduleJob(job, trigger);
 *
 * </pre>
 *
 * @see CronExpression
 * @see CronTrigger
 * @see ScheduleBuilder
 * @see SimpleScheduleBuilder
 * @see CalendarIntervalScheduleBuilder
 * @see TriggerBuilder
 *
 * @author jannisschalk
 */
public class CarbonAwareCronScheduleBuilder extends ScheduleBuilder<CarbonAwareCronTrigger> {

    private CronExpression cronExpression;
    private CarbonForecastApi carbonForecastApi;
    private int duration;
    private String deadlineCronExpression;
    private String location;

    private int misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;

    protected CarbonAwareCronScheduleBuilder(CronExpression cronExpression) {
        if (cronExpression == null) {
            throw new NullPointerException("cronExpression cannot be null");
        }
        this.cronExpression = cronExpression;
    }

    /**
     * Build the actual Trigger -- NOT intended to be invoked by end users, but
     * will rather be invoked by a TriggerBuilder which this ScheduleBuilder is
     * given to.
     *
     * @see TriggerBuilder#withSchedule(ScheduleBuilder)
     */
    @Override
    public MutableTrigger build() {

        CarbonAwareCronTriggerImpl ct = new CarbonAwareCronTriggerImpl();

        ct.setCronExpression(cronExpression);
        ct.setTimeZone(cronExpression.getTimeZone());
        ct.setMisfireInstruction(misfireInstruction);
        ct.setCarbonForecastApi(carbonForecastApi);
        ct.setDeadlineCronExpression(deadlineCronExpression);
        ct.setJobDurationInMinutes(duration);
        ct.setLocation(location);

        return ct;
    }

    /**
     * Location, which a mandatory parameter in the CarbonForecastApi.
     * */
    public CarbonAwareCronScheduleBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     *
     * */
    public CarbonAwareCronScheduleBuilder withCarbonForecastApi(CarbonForecastApi carbonForecastApi) {
        this.carbonForecastApi = carbonForecastApi;
        return this;
    }

    public CarbonAwareCronScheduleBuilder withJobDurationInMinutes(int duration) {
        this.duration = duration;
        return this;
    }

    public CarbonAwareCronScheduleBuilder withDeadlineCronExpression(String cronExpression) {
        this.deadlineCronExpression = cronExpression;
        return this;
    }

    /**
     * Create a CronScheduleBuilder with the given cron-expression string -
     * which is presumed to be a valid cron expression (and hence only a
     * RuntimeException will be thrown if it is not).
     *
     * @param cronExpression the cron expression string to base the schedule on.
     * @return the new CronScheduleBuilder
     * @throws RuntimeException wrapping a ParseException if the expression is invalid
     * @see CronExpression
     */
    public static CarbonAwareCronScheduleBuilder cronSchedule(String cronExpression) {
        try {
            return cronSchedule(new CronExpression(cronExpression));
        } catch (ParseException e) {
            // all methods of construction ensure the expression is valid by
            // this point...
            throw new RuntimeException("CronExpression '" + cronExpression
                    + "' is invalid.", e);
        }
    }

    /**
     * Create a CronScheduleBuilder with the given cron-expression.
     *
     * @param cronExpression the cron expression to base the schedule on.
     * @return the new CronScheduleBuilder
     * @see CronExpression
     */
    public static CarbonAwareCronScheduleBuilder cronSchedule(CronExpression cronExpression) {
        return new CarbonAwareCronScheduleBuilder(cronExpression);
    }

    /**
     * The <code>TimeZone</code> in which to base the schedule.
     *
     * @param timezone the time-zone for the schedule.
     * @return the updated CronScheduleBuilder
     * @see CronExpression#getTimeZone()
     */
    public CarbonAwareCronScheduleBuilder inTimeZone(TimeZone timezone) {
        cronExpression.setTimeZone(timezone);
        return this;
    }

    /**
     * If the Trigger misfires, use the
     * {@link Trigger#MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY} instruction.
     *
     * @return the updated CronScheduleBuilder
     * @see Trigger#MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
     */
    public CarbonAwareCronScheduleBuilder withMisfireHandlingInstructionIgnoreMisfires() {
        misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
        return this;
    }

    /**
     * If the Trigger misfires, use the
     * {@link CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING} instruction.
     *
     * @return the updated CronScheduleBuilder
     * @see CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING
     */
    public CarbonAwareCronScheduleBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }

    /**
     * If the Trigger misfires, use the
     * {@link CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW} instruction.
     *
     * @return the updated CronScheduleBuilder
     * @see CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
     */
    public CarbonAwareCronScheduleBuilder withMisfireHandlingInstructionFireAndProceed() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        return this;
    }
}
