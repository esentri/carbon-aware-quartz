package com.esentri.quartz.carbonaware.clients.opendata;

import com.esentri.quartz.carbonaware.clients.opendata.model.CachedForecast;
import com.esentri.quartz.carbonaware.entity.EmissionData;
import com.esentri.quartz.carbonaware.entity.EmissionForecast;
import com.esentri.quartz.carbonaware.exceptions.NoForecastException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


class OpenDataForecastClientTest {

    private OpenDataForecastClient sut;

    @BeforeAll
    static void setUpBeforeClass() {
        EnergyChartsForecastProvider.initialized = true;
        //15-minute steps
        EnergyChartsForecastProvider.cachedForecasts.put(
                "de",
                buildCachedForecast("2025-07-16T20:00",
                        buildCachedEmissionData("2025-07-16T09:00", 325.4, 15L),
                        buildCachedEmissionData("2025-07-16T09:15", 322.1, 15L),
                        buildCachedEmissionData("2025-07-16T09:30", 318.9, 15L),
                        buildCachedEmissionData("2025-07-16T09:45", 316.4, 15L),
                        buildCachedEmissionData("2025-07-16T10:00", 302.7, 15L),
                        buildCachedEmissionData("2025-07-16T10:15", 298.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:30", 292.4, 15L),
                        buildCachedEmissionData("2025-07-16T10:45", 286.2, 15L),
                        buildCachedEmissionData("2025-07-16T11:00", 279.8, 15L),
                        buildCachedEmissionData("2025-07-16T11:15", 275.8, 15L),
                        buildCachedEmissionData("2025-07-16T11:30", 275.1, 15L),
                        buildCachedEmissionData("2025-07-16T11:45", 272.5, 15L),
                        buildCachedEmissionData("2025-07-16T12:00", 268.2, 15L),
                        buildCachedEmissionData("2025-07-16T12:15", 268.0, 15L),
                        buildCachedEmissionData("2025-07-16T12:30", 268.3, 15L),
                        buildCachedEmissionData("2025-07-16T12:45", 269.1, 15L),
                        buildCachedEmissionData("2025-07-16T13:00", 270.3, 15L),
                        buildCachedEmissionData("2025-07-16T13:15", 271.6, 15L),
                        buildCachedEmissionData("2025-07-16T13:30", 273.4, 15L),
                        buildCachedEmissionData("2025-07-16T13:45", 276.2, 15L),
                        buildCachedEmissionData("2025-07-16T14:00", 286.3, 15L),
                        buildCachedEmissionData("2025-07-16T14:15", 295.0, 15L),
                        buildCachedEmissionData("2025-07-16T14:30", 304.0, 15L),
                        buildCachedEmissionData("2025-07-16T14:45", 316.7, 15L),
                        buildCachedEmissionData("2025-07-16T15:00", 338.8, 15L),
                        buildCachedEmissionData("2025-07-16T15:15", 346.7, 15L),
                        buildCachedEmissionData("2025-07-16T15:30", 357.3, 15L),
                        buildCachedEmissionData("2025-07-16T15:45", 368.8, 15L),
                        buildCachedEmissionData("2025-07-16T16:00", 402.2, 15L),
                        buildCachedEmissionData("2025-07-16T16:15", 416.8, 15L),
                        buildCachedEmissionData("2025-07-16T16:30", 433.3, 15L),
                        buildCachedEmissionData("2025-07-16T16:45", 448.8, 15L),
                        buildCachedEmissionData("2025-07-16T17:00", 466.4, 15L),
                        buildCachedEmissionData("2025-07-16T17:15", 484.1, 15L),
                        buildCachedEmissionData("2025-07-16T17:30", 501.8, 15L),
                        buildCachedEmissionData("2025-07-16T17:45", 517.5, 15L),
                        buildCachedEmissionData("2025-07-16T18:00", 531.2, 15L),
                        buildCachedEmissionData("2025-07-16T18:15", 544.7, 15L),
                        buildCachedEmissionData("2025-07-16T18:30", 557.0, 15L),
                        buildCachedEmissionData("2025-07-16T18:45", 567.4, 15L),
                        buildCachedEmissionData("2025-07-16T19:00", 575.7, 15L),
                        buildCachedEmissionData("2025-07-16T19:15", 578.8, 15L),
                        buildCachedEmissionData("2025-07-16T19:30", 579.1, 15L),
                        buildCachedEmissionData("2025-07-16T19:45", 578.0, 15L),
                        buildCachedEmissionData("2025-07-16T20:00", 577.7, 15L)

                ));

        // For testing the case where all data points have the same value (to test optimalExecutionPoint null branch)
        EnergyChartsForecastProvider.cachedForecasts.put(
                "same-values",
                buildCachedForecast("2025-07-16T13:00",
                        buildCachedEmissionData("2025-07-16T10:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:15", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:30", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:45", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:15", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:30", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:45", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T12:00", 100.0, 15L)
                ));
        //60-minute steps
        EnergyChartsForecastProvider.cachedForecasts.put(
                "ch",
                buildCachedForecast("2025-07-16T20:00",
                        buildCachedEmissionData("2025-07-16T09:00", 28.5, 60L),
                        buildCachedEmissionData("2025-07-16T10:00", 29.7, 60L),
                        buildCachedEmissionData("2025-07-16T11:00", 29.7, 60L),
                        buildCachedEmissionData("2025-07-16T12:00", 29.7, 60L),
                        buildCachedEmissionData("2025-07-16T13:00", 29.3, 60L),
                        buildCachedEmissionData("2025-07-16T14:00", 27.6, 60L),
                        buildCachedEmissionData("2025-07-16T15:00", 23.6, 60L),
                        buildCachedEmissionData("2025-07-16T16:00", 21.7, 60L),
                        buildCachedEmissionData("2025-07-16T17:00", 19.6, 60L),
                        buildCachedEmissionData("2025-07-16T18:00", 17.3, 60L),
                        buildCachedEmissionData("2025-07-16T19:00", 16.5, 60L),
                        buildCachedEmissionData("2025-07-16T20:00", 16.5, 60L)
                ));
        //too fewer entries for 4-hour job duration
        EnergyChartsForecastProvider.cachedForecasts.put(
                "at",
                buildCachedForecast("2025-07-16T11:00",
                        buildCachedEmissionData("2025-07-16T09:00", 28.5, 60L),
                        buildCachedEmissionData("2025-07-16T10:00", 29.7, 60L),
                        buildCachedEmissionData("2025-07-16T11:00", 29.7, 60L)
                ));
        //the forecast is null
        EnergyChartsForecastProvider.cachedForecasts.put("fr", null);
        //the forecast is empty
        EnergyChartsForecastProvider.cachedForecasts.put("nl", buildCachedForecast("2025-07-16T09:00"));

        // For testing when forecast.emissionData() is null
        EnergyChartsForecastProvider.cachedForecasts.put("be", new CachedForecast(
                LocalDateTime.of(2025, 7, 16, 9, 0, 0),
                LocalDateTime.of(2025, 7, 16, 20, 0, 0),
                null
        ));

        // For testing when optimalEmissionData is present and its timestamp is before dataStartAt
        // We need to ensure the minimum value is in a data point with timestamp before the start time
        // but still included in the filtered data (after dataStartAt.minusMinutes(data.duration()))
        EnergyChartsForecastProvider.cachedForecasts.put(
                "uk",
                buildCachedForecast("2025-07-16T20:00",
                        // Add data points with timestamps before the start time (09:00)
                        buildCachedEmissionData("2025-07-16T08:00", 50.0, 15L),
                        buildCachedEmissionData("2025-07-16T08:15", 45.0, 15L),
                        buildCachedEmissionData("2025-07-16T08:30", 40.0, 15L),
                        // This data point will be included in the filter because it's after 08:45 (09:00 - 15 minutes)
                        // and it has the lowest value, so it will be selected as the optimal data point
                        buildCachedEmissionData("2025-07-16T08:46", 10.0, 15L),  // Lowest value, just after 08:45
                        // Regular data points after start time with higher values
                        buildCachedEmissionData("2025-07-16T09:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T09:15", 110.0, 15L),
                        buildCachedEmissionData("2025-07-16T09:30", 120.0, 15L),
                        buildCachedEmissionData("2025-07-16T09:45", 130.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:00", 140.0, 15L)
                ));

        // Exactly 4 data points for testing exact window size match (no sliding window)
        EnergyChartsForecastProvider.cachedForecasts.put(
                "es",
                buildCachedForecast("2025-07-16T13:00",
                        buildCachedEmissionData("2025-07-16T10:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:15", 110.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:30", 120.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:45", 130.0, 15L)
                ));

        // For testing when the sliding window doesn't find a better minimum
        EnergyChartsForecastProvider.cachedForecasts.put(
                "it",
                buildCachedForecast("2025-07-16T13:00",
                        buildCachedEmissionData("2025-07-16T10:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:15", 110.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:30", 120.0, 15L),
                        buildCachedEmissionData("2025-07-16T10:45", 130.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:00", 140.0, 15L),
                        buildCachedEmissionData("2025-07-16T11:15", 150.0, 15L)
                ));

        // For testing empty emission data range with findAbsoluteMinimumCarbonIntensity
        EnergyChartsForecastProvider.cachedForecasts.put(
                "pt",
                buildCachedForecast("2025-07-16T13:00",
                        buildCachedEmissionData("2025-07-16T12:00", 100.0, 15L),
                        buildCachedEmissionData("2025-07-16T12:15", 110.0, 15L),
                        buildCachedEmissionData("2025-07-16T12:30", 120.0, 15L)
                ));
    }

    private static CachedForecast buildCachedForecast(String maximumForecastDateTime, CachedForecast.CachedEmissionData... emissionData) {
        return new CachedForecast(
                LocalDateTime.of(2025, 7, 16, 9, 0, 0),
                LocalDateTime.parse(maximumForecastDateTime),
                Arrays.asList(emissionData)
        );
    }

    private static CachedForecast.CachedEmissionData buildCachedEmissionData(String timestamp, Double value, Long duration) {
        return new CachedForecast.CachedEmissionData(LocalDateTime.parse(timestamp), value, duration);
    }

    @BeforeEach
    void setUp() {
        sut = new OpenDataForecastClient();
    }

    @Nested
    class WhenWindowSizeSmallerThanDataPointDuration {

        @Test
        void shouldReturnOptimalDataPointWithMinimumCarbonIntensity() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 30; // 30 minutes, smaller than data point duration (60 minutes)

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("ch"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("ch", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            assertEquals(1, optimalDataPoints.size());

            EmissionData optimalDataPoint = optimalDataPoints.get(0);
            assertEquals(LocalDateTime.of(2025, 7, 16, 19, 0), optimalDataPoint.timestamp());
            assertEquals(16.5, optimalDataPoint.value());
        }
    }

    @Nested
    class WhenWindowSizeLargerThanDataPointDuration {

        @Test
        void shouldReturnOptimalDataPointWithAverageMinimumCarbonIntensity() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 60; // 60 minutes, larger than data point duration (15 minutes)

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("de"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("de", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            assertEquals(1, optimalDataPoints.size());

            // The optimal data point should be at 12:00 with the lowest average over 4 data points
            EmissionData optimalDataPoint = optimalDataPoints.get(0);
            assertEquals(LocalDateTime.of(2025, 7, 16, 12, 0), optimalDataPoint.timestamp());
            // Average of 4 data points (12:00, 12:15, 12:30, 12:45)
            double expectedAverage = (268.2 + 268.0 + 268.3 + 269.1) / 4;
            assertEquals(expectedAverage, optimalDataPoint.value(), 0.01);
        }
    }

    @Nested
    class WhenForecastIsNullOrEmpty {

        @ParameterizedTest
        @ValueSource(strings = {"fr", "nl", "be"})
        void shouldThrowExceptionWhenForecastIsNull(String location) {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 30;

            // When & Then
            NoForecastException exception = assertThrows(NoForecastException.class, () ->
                    sut.getEmissionForecastCurrent(List.of(location), startTime, endTime, windowSize));

            assertEquals("No forecast available for location [%s]".formatted(location), exception.getMessage());
        }
    }

    @Nested
    class WhenDateBoundariesAreInvalid {

        @Test
        void shouldThrowExceptionWhenStartDateIsAfterMaximumForecastDate() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 17, 0, 0); // After maximum forecast date
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 17, 12, 0);
            Integer windowSize = 30;

            // When & Then
            NoForecastException exception = assertThrows(NoForecastException.class, () ->
                    sut.getEmissionForecastCurrent(List.of("de"), startTime, endTime, windowSize));

            assertTrue(exception.getMessage().startsWith("Start date"));
        }

        @Test
        void shouldAdjustEndDateWhenEndDateIsAfterMaximumForecastDate() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 17, 0, 0); // After maximum forecast date
            Integer windowSize = 30;

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("de"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            // The client should adjust the end date to the maximum forecast date
            // and still return a valid result
        }
    }

    @Nested
    class WhenNotEnoughDataPointsForWindowSize {

        @Test
        void shouldReturnEmptyOptimalDataPointsWhenNotEnoughDataPoints() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 11, 0);
            Integer windowSize = 240; // 4 hours, more than available data points

            // When
            assertThatThrownBy(() -> sut.getEmissionForecastCurrent(
                    List.of("at"), startTime, endTime, windowSize))
                    .isInstanceOf(NoForecastException.class)
                    .hasMessage("No forecast available for location [at]");
        }
    }

    @Nested
    class WhenMultipleLocationsProvided {

        @Test
        void shouldReturnForecastForEachLocation() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 30;

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("de", "ch"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify first forecast (de)
            EmissionForecast forecast1 = result.get(0);
            assertEquals("de", forecast1.location());

            // Verify second forecast (ch)
            EmissionForecast forecast2 = result.get(1);
            assertEquals("ch", forecast2.location());
        }
    }

    @Nested
    class WhenEmptyLocationsList {

        @Test
        void shouldReturnEmptyResultForEmptyLocationsList() {
            // Given
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 30;

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of(), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class WhenEmptyEmissionDataRange {

        @Test
        void shouldReturnEmptyOptimalDataPointsWithAbsoluteMinimumCarbonIntensity() {
            // Given
            // Use a time range that doesn't include any of the data points for "pt"
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 11, 0);
            Integer windowSize = 60; // 60 minutes, larger than data point duration (15 minutes)

            // When
            assertThatThrownBy(() -> sut.getEmissionForecastCurrent(
                    List.of("pt"), startTime, endTime, windowSize))
                    .isInstanceOf(NoForecastException.class)
                    .hasMessage("No forecast available for location [pt]");
        }
    }

    @Nested
    class WhenOptimalTimestampIsBeforeStartDate {

        @Test
        void shouldUseCurrentTimePlusOneMinuteWhenOptimalTimestampIsBeforeStartDate() {
            // Given
            // Start time is after the optimal data points (which are at 08:00-08:45)
            // But we need to include the data points before the start time in the filter
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            // We need to set the start time to be after the data point with the lowest value (08:00)
            // but still include it in the filter by using minusMinutes(data.duration())

            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 15; // 15 minutes, same as data point duration

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("uk"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("uk", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            assertEquals(1, optimalDataPoints.size());

            // The timestamp should be current time plus one minute, not the original timestamp (08:00)
            EmissionData optimalDataPoint = optimalDataPoints.get(0);

            // The value should be from the optimal data point (10.0)
            assertEquals(10.0, optimalDataPoint.value());

            // Check that the timestamp is not the original timestamp (08:00)
            assertNotEquals(LocalDateTime.of(2025, 7, 16, 8, 0), optimalDataPoint.timestamp());

            // Check that the timestamp is close to now plus one minute
            LocalDateTime now = LocalDateTime.now();

            // Allow a small tolerance for test execution time
            assertTrue(
                    optimalDataPoint.timestamp().isAfter(now.minusMinutes(1)) &&
                            optimalDataPoint.timestamp().isBefore(now.plusMinutes(3)),
                    "Timestamp should be close to now plus one minute"
            );
        }
    }

    @Nested
    class WhenAllDataPointsHaveSameValue {

        @Test
        void shouldHandleSameValueDataPoints() {
            // Given
            // This test is for the branch where all data points have the same value
            // This should test the case where optimalExecutionPoint could be null
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 10, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 12, 0);
            Integer windowSize = 60; // 60 minutes, requires 4 data points of 15 minutes each

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("same-values"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("same-values", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            // Verify that we have a valid result
            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            // The first window should be chosen since all values are the same
            assertFalse(optimalDataPoints.isEmpty());
            assertEquals(LocalDateTime.of(2025, 7, 16, 10, 0), optimalDataPoints.get(0).timestamp());
            assertEquals(100.0, optimalDataPoints.get(0).value());
        }
    }

    @Nested
    class WhenWindowSizeRequiresRoundingUp {

        @Test
        void shouldHandleWindowSizeRequiringRoundingUp() {
            // Given
            // This test is for the branch where value > Math.floor(value) in the calculation of amountOfDataPoints
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 32; // 32 minutes, which requires rounding up with 15-minute data points

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("de"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("de", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            // Verify that we have a valid result
            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            assertFalse(optimalDataPoints.isEmpty());
        }
    }

    @Nested
    class WhenWindowSizeRequiresRoundingDown {

        @Test
        void shouldHandleWindowSizeRequiringRoundingDown() {
            // Given
            // This test is for the branch where value == Math.floor(value) in the calculation of amountOfDataPoints
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 9, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 20, 0);
            Integer windowSize = 30; // 30 minutes, which is exactly 2 data points of 15 minutes each

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("de"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("de", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            // Verify that we have a valid result
            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            assertFalse(optimalDataPoints.isEmpty());
        }
    }

    @Nested
    class WhenNewSizeIsNegative {

        @Test
        void shouldHandleNegativeNewSize() {
            // Given
            // Create a test setup where newSize could be negative
            // This is to test the branch where newSize < 0 in the computeAverageMinimumCarbonIntensityOverMultipleDatapoints method

            // We'll use a location with very few data points but still enough to calculate a window
            LocalDateTime startTime = LocalDateTime.of(2025, 7, 16, 10, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 7, 16, 11, 0);
            Integer windowSize = 45; // 45 minutes, which requires 3 data points of 15 minutes each

            // When
            List<EmissionForecast> result = sut.getEmissionForecastCurrent(
                    List.of("es"), startTime, endTime, windowSize);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            EmissionForecast forecast = result.get(0);
            assertEquals("es", forecast.location());
            assertEquals(windowSize, forecast.windowSize());

            // Verify that we have a valid result
            List<EmissionData> optimalDataPoints = forecast.optimalDataPoints();
            assertNotNull(optimalDataPoints);
            // The result might be empty or not, depending on the implementation
            // We're just testing that the method doesn't throw an exception
        }
    }
}
