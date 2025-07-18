/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.clients.opendata;

import com.esentri.quartz.carbonaware.clients.opendata.model.CachedForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The EnergyChartsForecastProvider class handles the retrieval, processing,
 * and caching of forecasted carbon emission data for a specified set of locations.
 * This class interacts with an external API to fetch data and processes it into
 * a structured format for later use.
 * <p>
 * The class is designed to be used as a singleton and requires initialization
 * with a list of locations before usage. Once initialized, it fetches data from
 * the <a href="https://api.energy-charts.info/">Energy-Charts Open Data API</a> 
 * and populates its cache, which can be queried to access the latest data.
 * <p>
 * Thread safety and reusability are handled internally to ensure consistent behavior
 * during the lifecycle of this provider.
 *
 * @author jannisschalk
 */
public class EnergyChartsForecastProvider implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_API_URL_TEMPLATE = "https://api.energy-charts.info/co2eq?country=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(EnergyChartsForecastProvider.class);

    /**
     * Access level is "package private" for a better Unit-Test experience
     * Shouldn't be accessed or modified from outside of this class!
     * */
    static final Map<String, CachedForecast> cachedForecasts = new HashMap<>();
    /**
     * Access level is "package private" for a better Unit-Test experience
     * Shouldn't be accessed or modified from outside of this class!
     * */
    static String apiUrlTemplate;
    /**
     * Access level is "package private" for a better Unit-Test experience
     * Shouldn't be accessed or modified from outside of this class!
     * */
    static List<String> locations = new ArrayList<>();
    /**
     * Access level is "package private" for a better Unit-Test experience
     * Shouldn't be accessed or modified from outside of this class!
     * */
    static boolean initialized = false;

    private EnergyChartsForecastProvider() {
        // hide default public constructor
    }

    /**
     * Retrieves the cached forecast data for a specified location.
     *
     * @param location The location identifier for which to retrieve the forecast
     * @return The cached forecast data for the specified location, or null if not found
     */
    public static CachedForecast getForecast(String location) {
        return cachedForecasts.get(location);
    }

    /**
     * Initializes the EnergyChartsForecastProvider with a list of locations.
     * This method must be called before using the provider.
     * If the provider is already initialized, later calls will be ignored.
     *
     * @param locationsList List of location identifiers to initialize the provider with
     */
    public static void initialize(List<String> locationsList) {
        if (initialized) {
            LOGGER.warn("EnergyChartsForecastProvider is already initialized.");
            return;
        }
        locations = Collections.unmodifiableList(locationsList);
        
        // Only set the apiUrlTemplate if it hasn't been explicitly set already
        // This allows tests to set a custom URL before initialization
        if (apiUrlTemplate == null) {
            apiUrlTemplate = DEFAULT_API_URL_TEMPLATE;
        }
        
        updateCachedData();
        initialized = true;

        LOGGER.info("EnergyChartsForecastProvider initialized with locations: {}",
                locations.isEmpty() ? "" : String.join(", ", locations));
    }

    /**
     * Updates the cached forecast data for all initialized locations.
     * This method fetches fresh data from the API and updates the internal cache.
     * It is called during initialization and should only be called afterward by the {@link OpenDataUpdateJob}.
     *
     * @throws IllegalStateException if there is an error, fetching or processing the data
     */
    static void updateCachedData() {
        for (String location : locations) {
            String jsonData = null;
            try {
                // Make the HTTP request for each location
                jsonData = fetchDataFromApi(location);
                
                // Parse the JSON data
                List<CachedForecast.CachedEmissionData> cachedEmissionData = parseJsonToEmissionForecast(jsonData, location);
                
                // Check if the list is empty before trying to access its elements
                if (cachedEmissionData.isEmpty()) {
                    LOGGER.warn("No valid emission data found for location: {}", location);
                    // Don't continue, still update the cache with an empty forecast
                    cachedForecasts.put(location, new CachedForecast(
                            LocalDateTime.now(),
                            LocalDateTime.now(), // Use current time as maximum forecast timestamp
                            Collections.emptyList()
                    ));
                } else {
                    // Update the cache with the parsed data
                    LocalDateTime maximumForecastTimestamp = cachedEmissionData.get(cachedEmissionData.size() - 1).timestamp();
                    cachedForecasts.put(location, new CachedForecast(
                            LocalDateTime.now(),
                            maximumForecastTimestamp,
                            Collections.unmodifiableList(cachedEmissionData)
                    ));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Fetches forecast data from the API for a specific location.
     *
     * @param location The location identifier for which to fetch data
     * @return The raw JSON response from the API
     * @throws IOException if there is an error connecting to or reading from the API
     */
    private static String fetchDataFromApi(String location) throws IOException {
        URL url = new URL(apiUrlTemplate.formatted(location));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP-Fehler: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Parses the raw JSON data into a list of emission forecast data points.
     *
     * @param jsonData The raw JSON response from the API
     * @param location The location identifier for error logging purposes
     * @return List of parsed emission data points, or empty list if parsing fails
     */
    private static List<CachedForecast.CachedEmissionData> parseJsonToEmissionForecast(String jsonData,
                                                                                       String location) {
        try {
            // Extracting arrays using regular expressions
            List<Long> timestamps = extractLongArrayFromJson(jsonData);
            List<Double> co2eqValues = extractDoubleArrayFromJson(jsonData, "co2eq");
            List<Double> co2eqForecastValues = extractDoubleArrayFromJson(jsonData, "co2eq_forecast");


            if (timestamps.isEmpty()
                    || co2eqValues.isEmpty()
                    || co2eqForecastValues.isEmpty()
                    || timestamps.size() != co2eqValues.size()
                    || timestamps.size() != co2eqForecastValues.size()) {

                LOGGER.error("Invalid JSON format or empty arrays for location {}", location);
                return Collections.emptyList();
            }

            List<CachedForecast.CachedEmissionData> dataPoints = buildEmissionDataRecords(
                    timestamps,
                    co2eqValues,
                    co2eqForecastValues);

            if (dataPoints.isEmpty()) {
                LOGGER.warn("No valid data points for location {}", location);
                return Collections.emptyList();
            }

            return dataPoints;

        } catch (Exception e) {
            LOGGER.error("Error parsing JSON for location {}: {}", location, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Builds a list of emission data records from parsed timestamp and value arrays.
     *
     * @param timestamps          List of Unix timestamps for the data points
     * @param co2eqValues         List of CO2 equivalent values
     * @param co2eqForecastValues List of forecasted CO2 equivalent values
     * @return List of constructed emission data records
     */
    private static List<CachedForecast.CachedEmissionData> buildEmissionDataRecords(
            List<Long> timestamps,
            List<Double> co2eqValues,
            List<Double> co2eqForecastValues) {

        // Handle edge case: if there's only one timestamp, we can't calculate duration
        if (timestamps.size() <= 1) {
            return Collections.emptyList(); // Return empty list
        }

        List<CachedForecast.CachedEmissionData> dataPoints = new ArrayList<>();
        // Merging the arrays into EmissionData objects
        for (int i = 0; i < timestamps.size(); i++) {
            // Calculate duration based on adjacent timestamps
            long durationMinutes;
            if (i == timestamps.size() - 1) {
                // For the last element, calculate duration from the previous timestamp
                durationMinutes = Math.abs(Duration.between(
                        LocalDateTime.ofEpochSecond(timestamps.get(i - 1), 0, ZoneOffset.UTC),
                        LocalDateTime.ofEpochSecond(timestamps.get(i), 0, ZoneOffset.UTC))
                        .toMinutes());
            } else {
                // For other elements, calculate duration to the next timestamp
                durationMinutes = Math.abs(Duration.between(
                        LocalDateTime.ofEpochSecond(timestamps.get(i), 0, ZoneOffset.UTC),
                        LocalDateTime.ofEpochSecond(timestamps.get(i + 1), 0, ZoneOffset.UTC))
                        .toMinutes());
            }

            CachedForecast.CachedEmissionData data = new CachedForecast.CachedEmissionData(
                    LocalDateTime.ofEpochSecond(timestamps.get(i), 0, ZoneOffset.UTC),
                    co2eqValues.get(i) != null ? co2eqValues.get(i) : co2eqForecastValues.get(i),
                    durationMinutes
            );

            if (data.value() != null) {
                dataPoints.add(data);
            }
        }
        return dataPoints;
    }

    /**
     * Extracts an array of unix timestamp values from a JSON string.
     *
     * @param json The JSON string to parse
     * @return List of extracted Long values
     */
    private static List<Long> extractLongArrayFromJson(String json) {
        List<Long> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"unix_seconds\":\\s*\\[(.*?)]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            String[] items = arrayContent.split(",");

            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty() && !item.equals("null")) {
                    try {
                        result.add(Long.parseLong(item));
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Can not parse unix timestamp from string to long: {}", item);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Extracts an array of Double values from a JSON string.
     *
     * @param json      The JSON string to parse
     * @param arrayName The name of the array to extract
     * @return List of extracted Double values, with null for invalid or missing values
     */
    private static List<Double> extractDoubleArrayFromJson(String json, String arrayName) {
        List<Double> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"" + arrayName + "\":\\s*\\[(.*?)]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            String[] items = arrayContent.split(",");

            for (String item : items) {
                item = item.trim();
                if (item.isEmpty() || item.equals("null")) {
                    result.add(null);
                } else {
                    try {
                        result.add(Double.parseDouble(item));
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Can not parse double value from string: {}", item);
                        result.add(null);
                    }
                }
            }
        }

        return result;
    }
}
