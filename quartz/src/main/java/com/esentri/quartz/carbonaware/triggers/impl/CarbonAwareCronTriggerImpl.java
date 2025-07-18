/*
 * Portions of this file are based on the Quartz Scheduler project,
 * Copyright (c) Terracotta, Inc. Licensed under the Apache License, Version 2.0.
 *
 * Modifications and extensions Copyright (c) 2025 esentri AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esentri.quartz.carbonaware.triggers.impl;

import com.esentri.quartz.carbonaware.clients.rest.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.builders.CarbonAwareCronScheduleBuilder;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import com.esentri.quartz.carbonaware.util.Functions;
import org.quartz.*;
import org.quartz.impl.triggers.AbstractTrigger;
import org.quartz.impl.triggers.CoreTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Calendar;

/**
 * <p>
 * A concrete <code>{@link Trigger}</code> that is used to fire a <code>{@link org.quartz.JobDetail}</code>
 * at given moments in time, defined with Unix 'cron-like' definitions.
 * </p>
 *
 * <p>
 * Custom implementation inspired by Quartz's {@link org.quartz.impl.triggers.CronTriggerImpl}.
 * This class introduces additional functionality while maintaining
 * compatibility with existing Quartz scheduling mechanisms.
 * <br>
 * Based on Quartz Scheduler (Copyright (c) Terracotta, Inc.)
 * Licensed under Apache License 2.0.
 * </p>
 *
 * @author Terracotta, Inc.
 * @author jannisschalk, esentri AG (modifications & extensions)
 */
public class CarbonAwareCronTriggerImpl extends AbstractTrigger<CarbonAwareCronTrigger> implements CarbonAwareCronTrigger, CoreTrigger {

    /**
     * Required for serialization support. Introduced in Quartz 1.6.1 to
     * maintain compatibility after the introduction of hasAdditionalProperties
     * method.
     *
     * @see java.io.Serializable
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonAwareCronTriggerImpl.class);
    private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = CronExpression.MAX_YEAR;

    private CronExpression cronEx = null;
    private Date startTime = null;
    private Date endTime = null;
    private Date nextFireTime = null;
    private Date previousFireTime = null;
    private TimeZone timeZone = TimeZone.getDefault();

    private CarbonForecastApi carbonForecastApi;
    private int jobDurationInMinutes = 1;
    private CronExpression deadlineCronExpression;

    private CarbonAwareExecutionState carbonAwareExecutionState = CarbonAwareExecutionState.PENDING;
    private Date optimalExecutionTime;
    private Date configuredExecutionTime;
    private String carbonForecastLocation = "";
    private EmissionData currentForecast;

    /**
     * <p>
     * Create a plain CarbonAwareCronTriggerImpl, with a start time
     * </p>
     */
    public CarbonAwareCronTriggerImpl() {
        super();
        setStartTime(new Date());
    }

    @Override
    public Object clone() { //NOSONAR
        CarbonAwareCronTriggerImpl copy = (CarbonAwareCronTriggerImpl) super.clone();
        if (cronEx != null) {
            copy.setCronExpression(new CronExpression(cronEx));
            copy.setCarbonForecastApi(carbonForecastApi);
            copy.setDeadlineCronExpression(deadlineCronExpression.getCronExpression());
            copy.setJobDurationInMinutes(jobDurationInMinutes);
            copy.setCarbonAwareTriggerState(carbonAwareExecutionState);
        }
        return copy;
    }

    public void setCronExpression(String cronExpression) throws ParseException {
        TimeZone origTz = getTimeZone();
        this.cronEx = new CronExpression(cronExpression);
        this.cronEx.setTimeZone(origTz);
    }

    @Override
    public String getCronExpression() {
        return cronEx == null ? null : cronEx.getCronExpression();
    }

    /**
     * Set the CronExpression to the given one.  The TimeZone on the passed-in
     * CronExpression over-rides any that was already set on the Trigger.
     */
    public void setCronExpression(CronExpression cronExpression) {
        this.cronEx = cronExpression;
        this.timeZone = cronExpression.getTimeZone();
    }

    /**
     * <p>
     * Get the time at which the <code>CarbonAwareCronTriggerImpl</code> should start.
     * </p>
     */
    @Override
    public Date getStartTime() {
        return this.startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        Date eTime = getEndTime();
        if (eTime != null && eTime.before(startTime)) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time");
        }
        Calendar cl = Calendar.getInstance();
        cl.setTime(startTime);
        cl.set(Calendar.MILLISECOND, 0);

        this.startTime = cl.getTime();
    }

    /**
     * <p>
     * Get the time at which the <code>CarbonAwareCronTriggerImpl</code> should quit
     * repeating
     * </p>
     *
     * @see #getFinalFireTime()
     */
    @Override
    public Date getEndTime() {
        return this.endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        Date sTime = getStartTime();
        if (sTime != null && endTime != null && sTime.after(endTime)) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time");
        }

        this.endTime = endTime;
    }

    /**
     * <p>
     * Returns the next time at which the <code>Trigger</code> is scheduled to fire. If
     * the trigger will not fire again, <code>null</code> will be returned.  Note that
     * the time returned can possibly be in the past, if the time that was computed
     * for the trigger to next fire has already arrived, but the scheduler has not yet
     * been able to fire the trigger (which would likely be due to lack of resources
     * e.g. threads).
     * </p>
     *
     * <p>The value returned is not guaranteed to be valid until after the <code>Trigger</code>
     * has been added to the scheduler.
     * </p>
     *
     * @see TriggerUtils#computeFireTimesBetween(org.quartz.spi.OperableTrigger, org.quartz.Calendar, Date, Date)
     */
    @Override
    public Date getNextFireTime() {
        return this.nextFireTime;
    }

    /**
     * <p>
     * Returns the previous time at which the <code>CronTrigger</code>
     * fired. If the trigger has not yet fired, <code>null</code> will be
     * returned.
     */
    @Override
    public Date getPreviousFireTime() {
        return this.previousFireTime;
    }

    /**
     * <p>
     * Sets the next time at which the <code>CronTrigger</code> will fire.
     * <b>This method should not be invoked by client code.</b>
     * </p>
     */
    @Override
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    /**
     * <p>
     * Set the previous time at which the <code>CronTrigger</code> fired.
     * </p>
     *
     * <p>
     * <b>This method should not be invoked by client code.</b>
     * </p>
     */
    @Override
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }


    @Override
    public TimeZone getTimeZone() {

        if (cronEx != null) {
            return cronEx.getTimeZone();
        }

        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        return timeZone;
    }

    /**
     * <p>
     * Sets the time zone for which the <code>cronExpression</code> of this
     * <code>CarbonAwareCronTriggerImpl</code> will be resolved.
     * </p>
     *
     * <p>If {@link #setCronExpression(CronExpression)} is called after this
     * method, the TimeZon setting on the CronExpression will "win".  However
     * if {@link #setCronExpression(String)} is called after this method, the
     * time zone applied by this method will remain in effect, since the
     * String cron expression does not carry a time zone!
     */
    public void setTimeZone(TimeZone timeZone) {
        if (cronEx != null) {
            cronEx.setTimeZone(timeZone);
        }
        this.timeZone = timeZone;
    }

    /**
     * <p>
     * Returns the next time at which the <code>CarbonAwareCronTriggerImpl</code> will fire,
     * after the given time. If the trigger will not fire after the given time,
     * <code>null</code> will be returned.
     * </p>
     *
     * <p>
     * Note that the date returned is NOT validated against the related
     * org.quartz.Calendar (if any)
     * </p>
     */
    @Override
    public Date getFireTimeAfter(Date afterTime) {
        if (afterTime == null) {
            afterTime = new Date();
        }

        if (getStartTime().after(afterTime)) {
            afterTime = new Date(getStartTime().getTime() - 1000L);
        }

        if (getEndTime() != null && (afterTime.compareTo(getEndTime()) >= 0)) {
            return null;
        }

        Date pot = getTimeAfter(afterTime);
        if (getEndTime() != null && pot != null && pot.after(getEndTime())) {
            return null;
        }
        configuredExecutionTime = pot;

        if (carbonAwareExecutionState == CarbonAwareExecutionState.PENDING) {
            carbonAwareExecutionState = CarbonAwareExecutionState.READY;
            return pot;
        }

        if (carbonAwareExecutionState == CarbonAwareExecutionState.READY) {
            LOGGER.info("--- {} is about to determine better execution time... ---", getName());
            List<EmissionForecast> emissionForecasts = fetchCurrentForecast(
                    carbonForecastLocation,
                    pot,
                    deadlineCronExpression.getTimeAfter(pot),
                    jobDurationInMinutes);

            if(emissionForecasts == null || emissionForecasts.isEmpty()) {
                carbonAwareExecutionState = CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE;
                LOGGER.warn("Execution of Job won't be time shifted, because of missing carbon forecast.");
                return pot;
            }

            EmissionData emissionData = emissionForecasts.stream()
                    .filter(forecast -> carbonForecastLocation.equals(forecast.location()))
                    .map(EmissionForecast::optimalDataPoints)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .filter(data -> data.value() != null)
                    .min(Comparator.comparingDouble(EmissionData::value))
                    .orElse(null);

            if(emissionData == null) {
                carbonAwareExecutionState = CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE;
                LOGGER.warn("Execution of Job won't be time shifted. " +
                        "Either the current forecast received from the API does not match the configured location {}," +
                        " or there is no optimal data point.", carbonForecastLocation);
                return pot;
            }

            // store the current forecast for statistics
            this.currentForecast = emissionData;
            this.optimalExecutionTime = convertToDate(emissionData.timestamp(), timeZone);
            this.carbonAwareExecutionState = CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME;

            LOGGER.info("--- {} determined better execution time at {} ---", getName(), optimalExecutionTime);

            return optimalExecutionTime;
        }

        if (carbonAwareExecutionState == CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE
                || carbonAwareExecutionState == CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME) {
            carbonAwareExecutionState = CarbonAwareExecutionState.READY;
            return pot;
        }

        return pot;
    }

    private List<EmissionForecast> fetchCurrentForecast(String location, Date startTime, Date deadline, int durationInMinutes) {
        LocalDateTime startDate = convertToLocalDate(startTime, timeZone);
        LocalDateTime endDate = convertToLocalDate(deadline, timeZone);

        try{
            return carbonForecastApi.getEmissionForecastCurrent(List.of(location), startDate, endDate, durationInMinutes);
        } catch (Exception e) {
            LOGGER.warn("Exception was thrown during getEmissionForecast. Continue without emission forecast!: ", e);
            return List.of();
        }
    }

    private static LocalDateTime convertToLocalDate(Date date, TimeZone timeZone) {
        return Functions.convertDateToLocalDate(date, timeZone);
    }

    private static Date convertToDate(LocalDateTime date, TimeZone timeZone) {
        if (date == null) {
            return null;
        }
        ZoneOffset offset = timeZone.toZoneId().getRules().getOffset(LocalDateTime.now());
        return Date.from(date.toInstant(offset));
    }


    /**
     * <p>
     * NOT YET IMPLEMENTED: Returns the final time at which the
     * <code>CarbonAwareCronTrigger</code> will fire.
     * </p>
     *
     * <p>
     * Note that the return time *may* be in the past. and the date returned is
     * not validated against org.quartz.calendar
     * </p>
     */
    @Override
    public Date getFinalFireTime() {
        Date resultTime;
        if (getEndTime() != null) {
            resultTime = getTimeBefore(new Date(getEndTime().getTime() + 1000L));
        } else {
            resultTime = (cronEx == null) ? null : cronEx.getFinalFireTime();
        }

        if ((resultTime != null) && (getStartTime() != null) && (resultTime.before(getStartTime()))) {
            return null;
        }

        return resultTime;
    }

    /**
     * <p>
     * Determines whether or not the <code>CarbonAwareCronTriggerImpl</code> will start
     * again.
     * </p>
     */
    @Override
    public boolean mayFireAgain() {
        return (getNextFireTime() != null);
    }

    @Override
    protected boolean validateMisfireInstruction(int misfireInstruction) {
        return misfireInstruction >= MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY && misfireInstruction <= MISFIRE_INSTRUCTION_DO_NOTHING;
    }

    /**
     * <p>
     * Updates the <code>CarbonAwareCronTriggerImpl</code>'s state based on the
     * MISFIRE_INSTRUCTION_XXX that was selected when the <code>CarbonAwareCronTriggerImpl</code>
     * was created.
     * </p>
     *
     * <p>
     * If the misfire instruction is set to MISFIRE_INSTRUCTION_SMART_POLICY,
     * then the following scheme will be used: </p>
     * <ul>
     * <li>The instruction will be interpreted as <code>MISFIRE_INSTRUCTION_FIRE_ONCE_NOW</code>
     * </ul>
     */
    @Override
    public void updateAfterMisfire(org.quartz.Calendar cal) {
        int instr = getMisfireInstruction();

        if (instr == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
            return;

        if (instr == MISFIRE_INSTRUCTION_SMART_POLICY) {
            instr = MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        }

        if (instr == MISFIRE_INSTRUCTION_DO_NOTHING) {
            Date newFireTime = getFireTimeAfter(new Date());
            while (newFireTime != null && cal != null
                    && !cal.isTimeIncluded(newFireTime.getTime())) {
                newFireTime = getFireTimeAfter(newFireTime);
            }
            setNextFireTime(newFireTime);
        } else if (instr == MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) {
            setNextFireTime(new Date());
        }
    }

    /**
     * <p>
     * Called when the <code>{@link Scheduler}</code> has decided to 'fire'
     * the trigger (execute the associated <code>Job</code>), in order to
     * give the <code>Trigger</code> a chance to update itself for its next
     * triggering (if any).
     * </p>
     *
     * @see #executionComplete(JobExecutionContext, JobExecutionException)
     */
    @Override
    public void triggered(org.quartz.Calendar calendar) {
        previousFireTime = nextFireTime;
        nextFireTime = getFireTimeAfter(nextFireTime);

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }
    }

    /**
     * @see AbstractTrigger#updateWithNewCalendar(org.quartz.Calendar, long)
     */
    @Override
    public void updateWithNewCalendar(org.quartz.Calendar calendar, long misfireThreshold) {
        nextFireTime = getFireTimeAfter(previousFireTime);

        if (nextFireTime == null || calendar == null) {
            return;
        }

        Date now = new Date();
        while (nextFireTime != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

            nextFireTime = getFireTimeAfter(nextFireTime);

            if (nextFireTime == null)
                break;

            //avoid infinite loop
            // Use gregorian only because the constant is based on Gregorian
            Calendar c = new java.util.GregorianCalendar();
            c.setTime(nextFireTime);
            if (c.get(Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                nextFireTime = null;
            }

            if (nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if (diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                }
            }
        }
    }

    /**
     * <p>
     * Called by the scheduler at the time a <code>Trigger</code> is first
     * added to the scheduler, in order to have the <code>Trigger</code>
     * compute its first fire time, based on any associated calendar.
     * </p>
     *
     * <p>
     * After this method has been called, <code>getNextFireTime()</code>
     * should return a valid answer.
     * </p>
     *
     * @return the first time at which the <code>Trigger</code> will be fired
     * by the scheduler, which is also the same value <code>getNextFireTime()</code>
     * will return (until after the first firing of the <code>Trigger</code>).
     */
    @Override
    public Date computeFirstFireTime(org.quartz.Calendar calendar) {
        nextFireTime = getFireTimeAfter(new Date(getStartTime().getTime() - 1000L));

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }

        return nextFireTime;
    }

    @Override
    public String getExpressionSummary() {
        return cronEx == null ? null : cronEx.getExpressionSummary();
    }


    /**
     * Used by extensions of CarbonAwareCronTrigger to imply that there are additional
     * properties, specifically so that extensions can choose whether to be
     * stored as a serialized blob, or as a flattened CarbonAwareCronTrigger table.
     */
    @Override
    public boolean hasAdditionalProperties() {
        return false;
    }

    /**
     * Get a {@link ScheduleBuilder} that is configured to produce a
     * schedule identical to this trigger's schedule.
     *
     * @see #getTriggerBuilder()
     */
    @Override
    public ScheduleBuilder<CarbonAwareCronTrigger> getScheduleBuilder() {

        CarbonAwareCronScheduleBuilder cb = CarbonAwareCronScheduleBuilder.cronSchedule(getCronExpression())
                .inTimeZone(getTimeZone());

        int misfireInstruction = getMisfireInstruction();
        switch (misfireInstruction) {
            case MISFIRE_INSTRUCTION_SMART_POLICY:
                break;
            case MISFIRE_INSTRUCTION_DO_NOTHING:
                cb.withMisfireHandlingInstructionDoNothing();
                break;
            case MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:
                cb.withMisfireHandlingInstructionFireAndProceed();
                break;
            case MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY:
                cb.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            default:
                LOGGER.warn("Unrecognized misfire policy {}. Derived builder will use the default cron trigger behavior (MISFIRE_INSTRUCTION_FIRE_ONCE_NOW)", misfireInstruction);
        }

        return cb;
    }

    //--------------------------------------------------------------------------
    //
    // Computation Functions
    //
    //--------------------------------------------------------------------------

    protected Date getTimeAfter(Date afterTime) {
        return (cronEx == null) ? null : cronEx.getTimeAfter(afterTime);
    }

    /**
     * NOT YET IMPLEMENTED: Returns the time before the given time
     * that this <code>CarbonAwareCronTrigger</code> will fire.
     */
    protected Date getTimeBefore(Date eTime) {
        return (cronEx == null) ? null : cronEx.getTimeBefore(eTime);
    }

    //--------------------------------------------------------------------------
    //
    // Carbon Aware Functions
    //
    //--------------------------------------------------------------------------

    @Override
    public void setCarbonForecastApi(CarbonForecastApi carbonForecastApi) {
        this.carbonForecastApi = carbonForecastApi;
    }

    @Override
    public int getJobDuration() {
        return jobDurationInMinutes;
    }

    public void setJobDurationInMinutes(int jobDurationInMinutes) {
        this.jobDurationInMinutes = jobDurationInMinutes;
    }

    @Override
    public CronExpression getDeadlineCronExpression() {
        return deadlineCronExpression;
    }

    public void setDeadlineCronExpression(String deadlineCronExpression) {
        try {
            TimeZone origTz = getTimeZone();
            this.deadlineCronExpression = new CronExpression(deadlineCronExpression);
            this.deadlineCronExpression.setTimeZone(origTz);
        } catch (ParseException e) {
            throw new IllegalArgumentException("CronExpression '%s' is invalid!".formatted(deadlineCronExpression), e);
        }
    }

    @Override
    public CarbonAwareExecutionState getTriggerState() {
        return carbonAwareExecutionState;
    }


    public void setCarbonAwareTriggerState(CarbonAwareExecutionState carbonAwareExecutionState) {
        this.carbonAwareExecutionState = carbonAwareExecutionState;
    }

    @Override
    public void setLocation(String location) {
        this.carbonForecastLocation = location;
    }

    @Override
    public EmissionData getEmissionData() {
        return currentForecast;
    }

    @Override
    public Date getOptimalExecutionTime() {
        return optimalExecutionTime;
    }

    @Override
    public Date getConfiguredExecutionTime() {
        return configuredExecutionTime;
    }

    @Override
    public String getLocation() {
        return carbonForecastLocation;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cronEx, startTime, endTime, nextFireTime, previousFireTime, timeZone, carbonForecastApi, jobDurationInMinutes, deadlineCronExpression, carbonAwareExecutionState, optimalExecutionTime, configuredExecutionTime, carbonForecastLocation, currentForecast);
    }
}

