package com.esentri.quartz.carbonaware.clients.opendata.model;

import java.util.HashMap;
import java.util.Map;

public enum Location {

    DE("de", "Germany"),
    CH("ch", "Switzerland"),
    EU("eu", "European Union"),
    ALL("all", "Europe"),
    BA("ba", "Bosnia-Herzegovina"),
    AT("at", "Austria"),
    BE("be", "Belgium"),
    BG("bg", "Bulgaria"),
    CY("cy", "Cyprus"),
    CZ("cz", "Czech Republic"),
    DK("dk", "Denmark"),
    EE("ee", "Estonia"),
    ES("es", "Spain"),
    FI("fi", "Finland"),
    FR("fr", "France"),
    GE("ge", "Georgia"),
    GR("gr", "Greece"),
    HR("hr", "Croatia"),
    HU("hu", "Hungary"),
    IE("ie", "Ireland"),
    IT("it", "Italy"),
    LT("lt", "Lithuania"),
    LU("lu", "Luxembourg"),
    LV("lv", "Latvia"),
    MD("md", "Moldova"),
    ME("me", "Montenegro"),
    MK("mk", "North Macedonia"),
    NL("nl", "Netherlands"),
    NO("no", "Norway"),
    PL("pl", "Poland"),
    PT("pt", "Portugal"),
    RO("ro", "Romania"),
    RS("rs", "Serbia"),
    SE("se", "Sweden"),
    SI("si", "Slovenia"),
    SK("sk", "Slovak Republic"),
    UK("uk", "United Kingdom"),
    XK("xk", "Kosovo");

    private final String code;
    private final String displayName;
    private static final Map<String, Location> CODE_MAP = new HashMap<>();

    static {
        for (Location location : values()) {
            CODE_MAP.put(location.code, location);
        }
    }

    Location(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a Location enum based on the code (e.g. "de")
     *
     * @param code the code as a String (lowercase)
     * @return the corresponding Location enum or null if not found
     */
    public static Location fromCode(String code) {
        Location location = CODE_MAP.get(code);
        if (location == null) {
            throw new IllegalArgumentException("Invalid location code: " + code);
        }
        return CODE_MAP.get(code);
    }
}

