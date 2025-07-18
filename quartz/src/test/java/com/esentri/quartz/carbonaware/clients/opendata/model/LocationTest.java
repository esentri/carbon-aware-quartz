package com.esentri.quartz.carbonaware.clients.opendata.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void shouldHaveCorrectNumberOfLocations() {
        // The enum should have the expected number of values
        assertEquals(38, Location.values().length);
    }

    @Test
    void shouldHaveUniqueCodeForEachLocation() {
        // Each location should have a unique code
        Set<String> codes = new HashSet<>();
        for (Location location : Location.values()) {
            assertTrue(codes.add(location.getCode()), 
                    "Location code '" + location.getCode() + "' is duplicated");
        }
    }

    @Test
    void shouldHaveUniqueDisplayNameForEachLocation() {
        // Each location should have a unique display name
        Set<String> displayNames = new HashSet<>();
        for (Location location : Location.values()) {
            assertTrue(displayNames.add(location.getDisplayName()), 
                    "Location display name '" + location.getDisplayName() + "' is duplicated");
        }
    }

    @ParameterizedTest
    @MethodSource("provideLocationsWithCodesAndDisplayNames")
    void shouldHaveCorrectCodeAndDisplayName(Location location, String expectedCode, String expectedDisplayName) {
        assertEquals(expectedCode, location.getCode());
        assertEquals(expectedDisplayName, location.getDisplayName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "ch", "eu", "all", "at", "be", "uk"})
    void shouldReturnCorrectLocationFromValidCode(String code) {
        Location location = Location.fromCode(code);
        assertNotNull(location);
        assertEquals(code, location.getCode());
    }

    @Test
    void shouldThrowExceptionForInvalidCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.fromCode("invalid_code")
        );
        assertEquals("Invalid location code: invalid_code", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForNullCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.fromCode(null)
        );
        assertEquals("Invalid location code: null", exception.getMessage());
    }

    @Test
    void shouldReturnSameInstanceForRepeatedFromCodeCalls() {
        // The fromCode method should return the same enum instance for repeated calls with the same code
        Location location1 = Location.fromCode("de");
        Location location2 = Location.fromCode("de");
        assertSame(location1, location2);
    }

    @Test
    void shouldInitializeCodeMapWithAllLocations() {
        // Verify that all enum values can be retrieved via fromCode
        for (Location location : Location.values()) {
            assertEquals(location, Location.fromCode(location.getCode()));
        }
    }

    // Method source for parameterized test
    private static Stream<Arguments> provideLocationsWithCodesAndDisplayNames() {
        return Stream.of(
                Arguments.of(Location.DE, "de", "Germany"),
                Arguments.of(Location.CH, "ch", "Switzerland"),
                Arguments.of(Location.EU, "eu", "European Union"),
                Arguments.of(Location.ALL, "all", "Europe"),
                Arguments.of(Location.BA, "ba", "Bosnia-Herzegovina"),
                Arguments.of(Location.AT, "at", "Austria"),
                Arguments.of(Location.BE, "be", "Belgium"),
                Arguments.of(Location.BG, "bg", "Bulgaria"),
                Arguments.of(Location.CY, "cy", "Cyprus"),
                Arguments.of(Location.CZ, "cz", "Czech Republic"),
                Arguments.of(Location.DK, "dk", "Denmark"),
                Arguments.of(Location.EE, "ee", "Estonia"),
                Arguments.of(Location.ES, "es", "Spain"),
                Arguments.of(Location.FI, "fi", "Finland"),
                Arguments.of(Location.FR, "fr", "France"),
                Arguments.of(Location.GE, "ge", "Georgia"),
                Arguments.of(Location.GR, "gr", "Greece"),
                Arguments.of(Location.HR, "hr", "Croatia"),
                Arguments.of(Location.HU, "hu", "Hungary"),
                Arguments.of(Location.IE, "ie", "Ireland"),
                Arguments.of(Location.IT, "it", "Italy"),
                Arguments.of(Location.LT, "lt", "Lithuania"),
                Arguments.of(Location.LU, "lu", "Luxembourg"),
                Arguments.of(Location.LV, "lv", "Latvia"),
                Arguments.of(Location.MD, "md", "Moldova"),
                Arguments.of(Location.ME, "me", "Montenegro"),
                Arguments.of(Location.MK, "mk", "North Macedonia"),
                Arguments.of(Location.NL, "nl", "Netherlands"),
                Arguments.of(Location.NO, "no", "Norway"),
                Arguments.of(Location.PL, "pl", "Poland"),
                Arguments.of(Location.PT, "pt", "Portugal"),
                Arguments.of(Location.RO, "ro", "Romania"),
                Arguments.of(Location.RS, "rs", "Serbia"),
                Arguments.of(Location.SE, "se", "Sweden"),
                Arguments.of(Location.SI, "si", "Slovenia"),
                Arguments.of(Location.SK, "sk", "Slovak Republic"),
                Arguments.of(Location.UK, "uk", "United Kingdom"),
                Arguments.of(Location.XK, "xk", "Kosovo")
        );
    }
}