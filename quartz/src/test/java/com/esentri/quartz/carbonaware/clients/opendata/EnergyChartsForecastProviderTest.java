package com.esentri.quartz.carbonaware.clients.opendata;

import com.esentri.quartz.carbonaware.clients.opendata.model.CachedForecast;
import com.esentri.quartz.carbonaware.clients.opendata.model.Location;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EnergyChartsForecastProviderTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        // Reset static fields before each test
        EnergyChartsForecastProvider.cachedForecasts.clear();
        EnergyChartsForecastProvider.locations = new ArrayList<>();
        EnergyChartsForecastProvider.initialized = false;

        // Set up WireMock server
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        EnergyChartsForecastProvider.cachedForecasts.clear();
        EnergyChartsForecastProvider.locations = new ArrayList<>();
        EnergyChartsForecastProvider.initialized = false;

        // Stop WireMock server
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void getForecast_shouldReturnCachedForecast() {
        // Given
        CachedForecast mockForecast = new CachedForecast(
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24),
                Collections.emptyList()
        );
        EnergyChartsForecastProvider.cachedForecasts.put(Location.DE, mockForecast);

        // When
        CachedForecast result = EnergyChartsForecastProvider.getForecast(Location.DE);

        // Then
        assertEquals(mockForecast, result);
    }

    @Test
    void getForecast_shouldReturnNull_whenLocationNotCached() {
        // When
        CachedForecast result = EnergyChartsForecastProvider.getForecast(Location.ALL);

        // Then
        assertNull(result);
    }

    @Test
    void initialize_shouldInitializeProvider() {
        // Given
        List<String> locations = Arrays.asList("de", "fr");

        // Set up WireMock stubs for each location
        String mockResponseDe = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5,290.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=de"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseDe)));

        String mockResponseFr = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[200.5,190.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=fr"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseFr)));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // When
        EnergyChartsForecastProvider.initialize(locations);

        // Then
        assertTrue(EnergyChartsForecastProvider.initialized);
        assertEquals(Arrays.asList(Location.DE, Location.FR), EnergyChartsForecastProvider.locations);
        
        // Verify that the API URL template is set correctly
        assertEquals("http://localhost:8089/co2eq?country=%s", EnergyChartsForecastProvider.apiUrlTemplate);
        
        // Verify that the cache contains entries for the locations
        // Note: We're not verifying the actual HTTP requests because that's implementation-dependent
        // Instead, we're verifying that the provider is correctly initialized
        assertNotNull(EnergyChartsForecastProvider.cachedForecasts.get(Location.DE));
        assertNotNull(EnergyChartsForecastProvider.cachedForecasts.get(Location.FR));
    }

    @Test
    void initialize_shouldNotReinitialize_whenAlreadyInitialized() {
        // Given
        List<Location> initialLocations = Arrays.asList(Location.DE, Location.FR);
        List<String> newLocations = Arrays.asList("es", "it");

        EnergyChartsForecastProvider.locations = initialLocations;
        EnergyChartsForecastProvider.initialized = true;

        // Set up WireMock stubs for the new locations
        // These should not be called, but we set them up just in case
        String mockResponseEs = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[150.5,140.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=es"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseEs)));

        String mockResponseIt = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[100.5,90.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=it"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseIt)));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // When
        EnergyChartsForecastProvider.initialize(newLocations);

        // Then
        assertEquals(initialLocations, EnergyChartsForecastProvider.locations);

        // Verify that no requests were made
        verify(0, getRequestedFor(urlEqualTo("/co2eq?country=es")));
        verify(0, getRequestedFor(urlEqualTo("/co2eq?country=it")));
    }

    @Test
    void updateCachedData_shouldUpdateCache() {
        // Given
        EnergyChartsForecastProvider.locations = Arrays.asList(Location.DE, Location.FR);

        // Set up WireMock stubs for each location
        String mockResponseDe = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5,290.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=de"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseDe)));

        String mockResponseFr = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[200.5,190.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=fr"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseFr)));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // When
        EnergyChartsForecastProvider.updateCachedData();

        // Then
        assertNotNull(EnergyChartsForecastProvider.getForecast(Location.DE));
        assertNotNull(EnergyChartsForecastProvider.getForecast(Location.FR));

        // Verify that the requests were made
        verify(getRequestedFor(urlEqualTo("/co2eq?country=de"))
                .withHeader("Accept", equalTo("application/json")));
        verify(getRequestedFor(urlEqualTo("/co2eq?country=fr"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenJsonIsInvalid() throws Exception {
        // Given
        String invalidJson = "{invalid json}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, invalidJson, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenArraysAreEmpty() throws Exception {
        // Given
        String emptyArraysJson = "{\"unix_seconds\":[],\"co2eq\":[],\"co2eq_forecast\":[]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, emptyArraysJson, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenArraysHaveDifferentSizes() throws Exception {
        // Given
        String differentSizesJson = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5],\"co2eq_forecast\":[null,null]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, differentSizesJson, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void extractLongArrayFromJson_shouldExtractLongValues() throws Exception {
        // Given
        String json = "{\"unix_seconds\":[1626432000,1626435600,null,\"invalid\"]}";

        // Use reflection to access a private method
        Method extractMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("extractLongArrayFromJson", String.class);
        extractMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<Long> result = (List<Long>) extractMethod.invoke(null, json);

        // Then
        assertEquals(2, result.size());
        assertEquals(1626432000L, result.get(0));
        assertEquals(1626435600L, result.get(1));
    }

    @Test
    void extractDoubleArrayFromJson_shouldExtractDoubleValues() throws Exception {
        // Given
        String json = "{\"co2eq\":[300.5,290.2,null,\"invalid\"]}";

        // Use reflection to access a private method
        Method extractMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("extractDoubleArrayFromJson", String.class, String.class);
        extractMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<Double> result = (List<Double>) extractMethod.invoke(null, json, "co2eq");

        // Then
        assertEquals(4, result.size());
        assertEquals(300.5, result.get(0));
        assertEquals(290.2, result.get(1));
        assertNull(result.get(2));
        assertNull(result.get(3));
    }

    @Test
    void buildEmissionDataRecords_shouldBuildRecordsCorrectly() throws Exception {
        // Given
        List<Long> timestamps = Arrays.asList(1626432000L, 1626435600L, 1626439200L);
        List<Double> co2eqValues = Arrays.asList(300.5, 290.2, 280.0);
        List<Double> co2eqForecastValues = Arrays.asList(null, null, null);

        // Use reflection to access a private method
        Method buildMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("buildEmissionDataRecords", List.class, List.class, List.class);
        buildMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) buildMethod.invoke(null, timestamps, co2eqValues, co2eqForecastValues);

        // Then
        assertEquals(3, result.size());
        // Check the first data point
        assertEquals(300.5, result.get(0).value());
        // Check duration calculation
        assertEquals(60, result.get(0).duration()); // 3600 seconds = 60 minutes
    }

    @Test
    void buildEmissionDataRecords_shouldHandleNullValues() throws Exception {
        // Given
        List<Long> timestamps = Arrays.asList(1626432000L, 1626435600L);
        List<Double> co2eqValues = Arrays.asList(null, null);
        List<Double> co2eqForecastValues = Arrays.asList(300.5, 290.2);

        // Use reflection to access a private method
        Method buildMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("buildEmissionDataRecords", List.class, List.class, List.class);
        buildMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) buildMethod.invoke(null, timestamps, co2eqValues, co2eqForecastValues);

        // Then
        assertEquals(2, result.size());
        // Check that forecast values are used when co2eq values are null
        assertEquals(300.5, result.get(0).value());
        assertEquals(290.2, result.get(1).value());
    }

    @Test
    void fetchDataFromApi_shouldReturnJsonData_whenResponseCodeIs200() throws Exception {
        // Given
        String mockResponse = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5,290.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=de"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // Use reflection to access a private method
        Method fetchMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("fetchDataFromApi", Location.class);
        fetchMethod.setAccessible(true);

        // When
        String result = (String) fetchMethod.invoke(null, Location.DE);

        // Then
        assertEquals(mockResponse, result);

        // Verify that the request was made
        verify(getRequestedFor(urlEqualTo("/co2eq?country=de"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void fetchDataFromApi_shouldThrowException_whenResponseCodeIsNot200() throws Exception {
        // Given
        stubFor(get(urlEqualTo("/co2eq?country=fr"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Not Found")));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // Use reflection to access a private method
        Method fetchMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("fetchDataFromApi", Location.class);
        fetchMethod.setAccessible(true);

        // When & Then
        try {
            fetchMethod.invoke(null, Location.FR);
            fail("Expected an InvocationTargetException to be thrown");
        } catch (InvocationTargetException e) {
            // When using reflection, exceptions thrown by the method are wrapped in InvocationTargetException
            assertInstanceOf(IOException.class, e.getCause(), "Expected cause to be IOException, but was " + e.getCause().getClass().getName());
            assertEquals("HTTP-Fehler: 404", e.getCause().getMessage());
        }

        // Verify that the request was made
        verify(getRequestedFor(urlEqualTo("/co2eq?country=fr"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void updateCachedData_shouldThrowIllegalStateException_whenIOExceptionOccurs() {
        // Given
        EnergyChartsForecastProvider.locations = List.of(Location.DE);

        // Set up WireMock to return a connection error
        stubFor(get(urlEqualTo("/co2eq?country=de"))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // Set the apiUrlTemplate to point to the WireMock server
        EnergyChartsForecastProvider.apiUrlTemplate = "http://localhost:8089/co2eq?country=%s";

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, EnergyChartsForecastProvider::updateCachedData);

        // Verify that the exception has an IOException as its cause
        assertInstanceOf(IOException.class, exception.getCause());

        // Verify that the request was made
        verify(getRequestedFor(urlEqualTo("/co2eq?country=de"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenDataPointsAreEmpty() throws Exception {
        // Given
        // Create a JSON where all values are null, which will result in empty dataPoints
        String jsonWithNullValues = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[null,null],\"co2eq_forecast\":[null,null]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, jsonWithNullValues, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenExceptionOccurs() throws Exception {
        // Given
        // Create a JSON that will cause an exception in the buildEmissionDataRecords method
        // This JSON has valid arrays but will cause an ArrayIndexOutOfBoundsException when accessing timestamps[i-1]
        // for the last element (when calculating duration)
        String jsonWithSingleTimestamp = "{\"unix_seconds\":[1626432000],\"co2eq\":[300.5],\"co2eq_forecast\":[400.5]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, jsonWithSingleTimestamp, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenCo2eqValuesIsEmpty() throws Exception {
        // Given
        // Create a JSON where timestamps is not empty but co2eqValues is empty
        // This specifically targets the co2eqValues.isEmpty() branch
        String jsonWithEmptyCo2eq = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[],\"co2eq_forecast\":[null,null]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, jsonWithEmptyCo2eq, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToEmissionForecast_shouldReturnEmptyList_whenCo2eqForecastValuesIsEmpty() throws Exception {
        // Given
        // Create a JSON where timestamps and co2eqValues are not empty but co2eqForecastValues is empty
        // This specifically targets the co2eqForecastValues.isEmpty() branch
        String jsonWithEmptyCo2eqForecast = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5,290.2],\"co2eq_forecast\":[]}";

        // Use reflection to access a private method
        Method parseMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("parseJsonToEmissionForecast", String.class, Location.class);
        parseMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) parseMethod.invoke(null, jsonWithEmptyCo2eqForecast, Location.DE);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void buildEmissionDataRecords_shouldHandleAllNullValues() throws Exception {
        // Given
        List<Long> timestamps = Arrays.asList(1626432000L, 1626435600L);
        List<Double> co2eqValues = Arrays.asList(null, null);
        List<Double> co2eqForecastValues = Arrays.asList(null, null);

        // Use reflection to access a private method
        Method buildMethod = EnergyChartsForecastProvider.class.getDeclaredMethod("buildEmissionDataRecords", List.class, List.class, List.class);
        buildMethod.setAccessible(true);

        // When
        @SuppressWarnings("unchecked")
        List<CachedForecast.CachedEmissionData> result = (List<CachedForecast.CachedEmissionData>) buildMethod.invoke(null, timestamps, co2eqValues, co2eqForecastValues);

        // Then
        assertTrue(result.isEmpty(), "Result should be empty when all values are null");
    }
}
