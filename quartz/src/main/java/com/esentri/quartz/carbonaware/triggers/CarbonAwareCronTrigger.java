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

package com.esentri.quartz.carbonaware.triggers;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * The public interface for inspecting settings specific to a CarbonAwareCronTrigger,
 * which is used to fire a <code>{@link Job}</code>
 * between the given moments in time, where the power grid produces a minimum oc CO2.
 * <br>
 * <br>
 * Therefore, a CarbonForecast is required. To get this forecast data you have to implement
 * a {@link CarbonForecastApi}. This is a minimal Subset of the
 * <a href="https://greensoftware.foundation/projects"><b>Green Software Foundation CarbonAware SDK</b></a>
 * <br>
 * <br>
 * <p>
 * For those unfamiliar with "cron", this means being able to create a firing
 * schedule such as: "At 8:00am every Monday through Friday".
 * </p>
 * <br>
 * <br>
 * <p>
 * The format of a "Cron-Expression" string is documented on the 
 * {@link CronExpression} class.
 * </p>
 * 
 * <p>
 * Here are some full examples: </p>
 * <table>
 * <caption>Examples of cron expressions and their meanings.</caption>
 * <tr>
 * <th>Expression</th>
 * <th>&nbsp;</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td><code>"0 0 12 * * ?"</code></td>
 * <td>&nbsp;</td>
 * <td><code>Fire at 12pm (noon) every day</code></td>
 * </tr>
 * <tr>
 * <td><code>"0 15 10 ? * *"</code></td>
 * <td>&nbsp;</td>
 * <td><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td><code>"0 15 10 * * ?"</code></td>
 * <td>&nbsp;</td>
 * <td><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td><code>"0 15 10 * * ? *"</code></td>
 * <td>&nbsp;</td>
 * <td><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td><code>"0 15 10 * * ? 2005"</code></td>
 * <td>&nbsp;</td>
 * <td><code>Fire at 10:15am every day during the year 2005</code>
 * </td>
 * </tr>
 * 
 * <p>
 * Pay attention to the effects of '?' and '*' in the day-of-week and
 * day-of-month fields!
 * </p>
 * 
 * <p>
 * <b>NOTES:</b>
 * </p>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in on of these fields).
 * </li>
 * <li>Be careful when setting fire times between mid-night and 1:00 AM -
 * "daylight savings" can cause a skip or a repeat depending on whether the
 * time moves back or jumps forward.</li>
 * </ul>
 * 
 * @see CronScheduleBuilder
 * @see TriggerBuilder
 * 
 * @author jannisschalk
 */
public interface CarbonAwareCronTrigger extends Trigger {

    long serialVersionUID = -8644953146451592766L;
    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CarbonAwareCronTrigger}</code> wants to be fired now
     * by <code>Scheduler</code>.
     * </p>
     */
   int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;
    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CarbonAwareCronTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time (taking into account any associated <code>{@link Calendar}</code>,
     * but it does not want to be fired now.
     * </p>
     */
    int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

    String getCronExpression();

    /**
     * <p>
     * Returns the time zone for which the <code>cronExpression</code> of
     * this <code>CronTrigger</code> will be resolved.
     * </p>
     */
    TimeZone getTimeZone();

    String getExpressionSummary();

    TriggerBuilder<CarbonAwareCronTrigger> getTriggerBuilder();

    CarbonAwareExecutionState getTriggerState();

    void setCarbonForecastApi(CarbonForecastApi carbonForecastApi);

    CronExpression getDeadlineCronExpression();

    int getJobDuration();

    void setLocation(String location);
}
