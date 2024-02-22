package com.esentri.quartz.carbonaware.triggers.impl;

import com.esentri.quartz.carbonaware.clients.CarbonForecastApi;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.triggers.states.CarbonAwareExecutionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarbonAwareCronTriggerImplTest {

    private static final ZoneOffset ZONE_OFFSET = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    private static final String FORECAST_LOCATION = "de";
    private static final int JOB_DURATION = 10;
    @Mock
    private CarbonForecastApi carbonForecastApi;

    private CarbonAwareCronTriggerImpl sut;

    private final Calendar calendar = Calendar.getInstance();

    private CronExpression startCronExpression;
    private CronExpression deadlineCronExpression;

    @BeforeEach
     void setUp() throws Exception {
        initSut();

        this.startCronExpression = new CronExpression(sut.getCronExpression());
        this.deadlineCronExpression = sut.getDeadlineCronExpression();
    }

    private void initSut() throws ParseException {
        sut = new CarbonAwareCronTriggerImpl();
        sut.setCarbonForecastApi(carbonForecastApi);
        sut.setCronExpression("20 0/1 * ? * *");
        sut.setDeadlineCronExpression("50 0/1 * ? * *");
        sut.setTimeZone(TimeZone.getDefault());
        sut.setLocation(FORECAST_LOCATION);
        // should be a Duration in Minutes as expected by CarbonAware SDK.
        // In this this Test we use SECONDS
        sut.setJobDurationInMinutes(JOB_DURATION);
    }

    @Test
     void shouldNotThrowAnyException_IfProvidedAfterTimeIsNull() {
        assertThatCode(() -> sut.getFireTimeAfter(null))
                .doesNotThrowAnyException();
    }

    @Test
     void shouldReturnNull_IfEndDateIsPresnet_AndEndDateIsAfterProvidedTime() {
        sut.setEndTime(Date.from(Instant.now()));

        Date result = sut.getFireTimeAfter(Date.from(Instant.now().plus(5, ChronoUnit.HOURS)));

        assertThat(result).isNull();
    }

    @Test
     void shouldReturnDateFromStartCronExpression_WhenTriggerStateIsPending() {
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.PENDING);
        Date cronStartTime = calendar.getTime();

        Date result = sut.getFireTimeAfter(cronStartTime);

        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(cronStartTime));

        verifyNoInteractions(carbonForecastApi);
    }

    @Test
     void shouldSetTriggerStateToReady_AfterCallingGetFireTimeTheFirstTime() {
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.PENDING);
        Date cronStartTime = calendar.getTime();

        sut.getFireTimeAfter(cronStartTime);

        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.READY);
    }

    @Test
     void shouldCallCarbonForecastApi_WhenStateIsReady() {
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();


        sut.getFireTimeAfter(startDate);

        Date cronStartTime = startCronExpression.getTimeAfter(startDate);
        Date cronDeadlineTime = deadlineCronExpression.getTimeAfter(cronStartTime);

        verify(carbonForecastApi).getEmissionForecastCurrent(
                list(FORECAST_LOCATION),
                LocalDateTime.ofInstant(cronStartTime.toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(cronDeadlineTime.toInstant(), ZoneId.systemDefault()),
                JOB_DURATION);
    }

    @Test
     void shouldReturnCronStartTime_WhenStateIsReady_AndReturnedCarbonForecastIsNull() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(null);

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        Date result = sut.getFireTimeAfter(startDate);

        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(startDate));
    }

    @Test
     void shouldChangeTriggerStateToCarbonDataUnavailable_WhenStateIsReady_AndReturnedCarbonForecastIsNull() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(null);

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        sut.getFireTimeAfter(startDate);


        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE);
    }

    @Test
     void shouldReturnCronStartTime_WhenStateIsReady_AndReturnedCarbonForecastIsEmpty() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(List.of());

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        Date result = sut.getFireTimeAfter(startDate);

        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(startDate));
    }

    @Test
     void shouldChangeTriggerStateToCarbonDataUnavailable_WhenStateIsReady_AndReturnedCarbonForecastIsEmpty() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list());

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        sut.getFireTimeAfter(startDate);


        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE);
    }

    @Test
     void shouldReturnCronStartTime_WhenStateIsReady_AndReturnedCarbonForecastHasNullAsOptimalDataPoints() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, null)));

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        Date result = sut.getFireTimeAfter(startDate);

        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(startDate));
    }

    @Test
     void shouldChangeTriggerStateToCarbonDataUnavailable_WhenStateIsReady_AndReturnedCarbonForecastHasNullAsOptimalDataPoints() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, null)));

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        sut.getFireTimeAfter(startDate);


        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE);
    }

    @Test
     void shouldReturnCronStartTime_WhenStateIsReady_AndReturnedCarbonForecastHasEmptyOptimalDataPoints() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, List.of())));

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        Date result = sut.getFireTimeAfter(startDate);

        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(startDate));
    }

    @Test
     void shouldChangeTriggerStateToCarbonDataUnavailable_WhenStateIsReady_AndReturnedCarbonForecastHasEmptyOptimalDataPoints() {
        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, List.of())));

        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);
        Date startDate = calendar.getTime();

        sut.getFireTimeAfter(startDate);


        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.CARBON_DATA_UNAVAILABLE);
    }

    @Test
     void shouldReturnOptimalExecutionTimeFromForecast_WhenStateIsReady_AndReturnedCarbonForecastIsPresent() {
        Date startDate = calendar.getTime();
        LocalDateTime optimalExecutionDate = LocalDateTime.ofInstant(startDate.toInstant().plus(5, ChronoUnit.SECONDS), ZoneId.systemDefault());

        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, list(
                        new EmissionDataImpl(optimalExecutionDate, 22.7)
                ))));
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        Date result = sut.getFireTimeAfter(startDate);


        assertThat(result.toInstant()).isEqualTo(optimalExecutionDate.toInstant(ZONE_OFFSET));
    }

    @Test
     void shouldChangeTriggerStateToDeterminedBetterExecutionTime_WhenStateIsReady_AndReturnedCarbonForecastIsPresent() {
        Date startDate = calendar.getTime();
        LocalDateTime optimalExecutionDate = LocalDateTime.ofInstant(startDate.toInstant().plus(5, ChronoUnit.SECONDS), ZoneId.systemDefault());

        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, list(
                        new EmissionDataImpl(optimalExecutionDate, 22.7)
                ))));
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        sut.getFireTimeAfter(startDate);


        assertThat(sut.getTriggerState()).isEqualTo(CarbonAwareExecutionState.DETERMINED_BETTER_EXECUTION_TIME);
    }

    @Test
     void shouldReturnCronStartDate_WhenStateIsReady_AndReturnedCarbonForecastIsPresentForWrongLocation() {
        Date startDate = calendar.getTime();
        LocalDateTime optimalExecutionDate = LocalDateTime.ofInstant(startDate.toInstant().plus(5, ChronoUnit.SECONDS), ZoneId.systemDefault());

        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl("fr", JOB_DURATION, list(
                        new EmissionDataImpl(optimalExecutionDate, 22.7)
                ))));
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        Date result = sut.getFireTimeAfter(startDate);


        assertThat(result).isEqualTo(startCronExpression.getTimeAfter(startDate));
    }

    @Test
     void shouldReturnStartDateFromFirstOptimalExecutionPoint_WhenStateIsReady_AndReturnedCarbonForecastIsPresent() {
        Date startDate = calendar.getTime();
        LocalDateTime optimalExecutionDate = LocalDateTime.ofInstant(startDate.toInstant().plus(5, ChronoUnit.SECONDS), ZoneId.systemDefault());

        when(carbonForecastApi.getEmissionForecastCurrent(any(), any(), any(), any()))
                .thenReturn(list(new EmissionForecastImpl(FORECAST_LOCATION, JOB_DURATION, list(
                        new EmissionDataImpl(optimalExecutionDate, 999.9),
                        new EmissionDataImpl(optimalExecutionDate.plusSeconds(2), 2.8),
                        new EmissionDataImpl(optimalExecutionDate.plusSeconds(4), 22.7),
                        null
                ))));
        sut.setCarbonAwareTriggerState(CarbonAwareExecutionState.READY);

        Date result = sut.getFireTimeAfter(startDate);

        assertThat(result.toInstant()).isEqualTo(optimalExecutionDate.toInstant(ZONE_OFFSET));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ----
    // ---- CarbonForecast Implementations
    // ----
    // -----------------------------------------------------------------------------------------------------------------
    private static class EmissionForecastImpl implements EmissionForecast {

        String location;
        Integer windowSize;
        List<EmissionData> emissionData;

        public EmissionForecastImpl(String location, Integer windowSize, List<EmissionData> emissionData) {
            this.location = location;
            this.windowSize = windowSize;
            this.emissionData = emissionData;
        }

        @Override
        public String getLocation() {
            return location;
        }

        @Override
        public Integer getWindowSize() {
            return windowSize;
        }

        @Override
        public List<EmissionData> getOptimalDataPoints() {
            return emissionData;
        }
    }

    private static class EmissionDataImpl implements EmissionData {

        public EmissionDataImpl(LocalDateTime timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        LocalDateTime timestamp;
        Double value;

        @Override
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public Double getValue() {
            return value;
        }
    }


}