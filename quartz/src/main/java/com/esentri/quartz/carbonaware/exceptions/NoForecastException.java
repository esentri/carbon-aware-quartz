/*
 * Copyright (c) 2025 esentri AG
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package com.esentri.quartz.carbonaware.exceptions;

/**
 * Exception thrown when no carbon emission forecast data is available for a requested location
 * or when the requested forecast dates are outside the available forecast window.
 *
 * @author jannisschalk
 */
public class NoForecastException extends RuntimeException {
    /**
     * Constructs a new NoForecastException with the specified detail message.
     *
     * @param message the detail message explaining why the forecast data is not available
     */
    public NoForecastException(String message) {
        super(message);
    }
}
