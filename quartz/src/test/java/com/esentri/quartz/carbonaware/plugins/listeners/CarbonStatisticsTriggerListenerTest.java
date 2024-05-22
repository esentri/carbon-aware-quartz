package com.esentri.quartz.carbonaware.plugins.listeners;

import com.esentri.quartz.carbonaware.testsupport.CarbonForecastClient;
import com.esentri.quartz.carbonaware.testsupport.EmissionDataImpl;
import com.esentri.quartz.carbonaware.testsupport.PersistenceClient;
import com.esentri.quartz.carbonaware.triggers.CarbonAwareCronTrigger;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarbonStatisticsTriggerListenerTest {

    private static final String PERSISTENCE_CLIENT_REF = PersistenceClient.class.getName();
    private static final String REST_CLIENT_REF = CarbonForecastClient.class.getName();

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2024, 3, 19, 0, 0)
                    .toInstant(ZoneOffset.UTC),
            ZoneId.systemDefault());
    @Mock
    private Scheduler scheduler;
    @Mock
    private JobExecutionContext context;
    @Mock
    private CarbonAwareCronTrigger trigger;

    private CarbonStatisticsTriggerListener sut;

    private final JobDetailImpl jobDetail = createJobDetail();
    private final TimeZone timeZone = TimeZone.getDefault();
    private CarbonAwareExecutionState triggerState = CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME;
    private final Date configuredExecutionTime = Date.from(FIXED_CLOCK.instant());
    private final Date optimalExecutionTime = Date.from(FIXED_CLOCK.instant().plus(5, ChronoUnit.HOURS));
    private final int duration = 10;
    private final String location = "de";
    private final EmissionDataImpl emissionData = new EmissionDataImpl(LocalDateTime.now(FIXED_CLOCK).plusHours(5), 20.78);

    @BeforeEach
    void setUp() {
        when(context.getJobDetail()).then(__ -> jobDetail);
        when(context.getFireInstanceId()).then(__ -> "0000_fire-instance_ID");

        when(trigger.getTimeZone()).then(__ -> timeZone);
        when(trigger.getTriggerState()).then(__ -> triggerState);
        when(trigger.getConfiguredExecutionTime()).then(__ -> configuredExecutionTime);
        when(trigger.getOptimalExecutionTime()).then(__ -> optimalExecutionTime);
        when(trigger.getLocation()).then(__ -> location);
        when(trigger.getJobDuration()).then(__ -> duration);
        when(trigger.getEmissionData()).then(__ -> emissionData);

        sut = new CarbonStatisticsTriggerListener(
                PERSISTENCE_CLIENT_REF,
                REST_CLIENT_REF,
                false);
    }

    private static JobDetailImpl createJobDetail() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setGroup("any-job-group");
        jobDetail.setName("my-job");
        return jobDetail;
    }


    @Test
    void shouldPersistCarbonData() {
        sut.triggerFired(trigger, context);

        PersistenceClient persistenceClient = (PersistenceClient) sut.getPersistenceClient();
        assertThat(persistenceClient.persistedObjects).hasSize(1);
    }

    @Test
    void shouldNotPersistData_IfTriggerHasWrongState() {
        triggerState = CarbonAwareExecutionState.PENDING;

        sut.triggerFired(trigger, context);

        PersistenceClient persistenceClient = (PersistenceClient) sut.getPersistenceClient();
        assertThat(persistenceClient.persistedObjects).hasSize(0);
    }

    @Test
    void shouldNotPersistData_IfTriggerHasWrongType() {
        triggerState = CarbonAwareExecutionState.PENDING;

        sut.triggerFired(new CronTriggerImpl(), context);

        PersistenceClient persistenceClient = (PersistenceClient) sut.getPersistenceClient();
        assertThat(persistenceClient.persistedObjects).hasSize(0);
    }

    @Test
    void shouldPersistDataWithCorrectValues() {
        sut.triggerFired(trigger, context);

        PersistenceClient persistenceClient = (PersistenceClient) sut.getPersistenceClient();
        assertThat(persistenceClient.persistedObjects)
                .singleElement()
                .hasFieldOrPropertyWithValue("jobExecutionId", "0000_fire-instance_ID")
                .hasFieldOrPropertyWithValue("jobName", "my-job")
                .hasFieldOrPropertyWithValue("jobGroupName", "any-job-group")
                .hasFieldOrPropertyWithValue("configuredTimestamp", LocalDateTime.of(2024, 3, 19, 0, 0).toInstant(ZoneOffset.UTC))
                .hasFieldOrPropertyWithValue("executionTimestamp", LocalDateTime.of(2024, 3, 19, 5, 0).toInstant(ZoneOffset.UTC))
                .hasFieldOrPropertyWithValue("jobDuration", 10)
                .hasFieldOrPropertyWithValue("carbonIntensityForConfiguredTimestamp", 63.7)
                .hasFieldOrPropertyWithValue("carbonIntensityForRescheduledTimestamp", 20.78)
                .hasFieldOrPropertyWithValue("location", "de")
                .hasFieldOrPropertyWithValue("dryRun", false);
    }


}