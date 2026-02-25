package com.booking.config;

/**
 * Central registry of all API endpoint paths derived from the OpenAPI specification.
 * These are contract constants — they do not vary between environments.
 * Environment-specific values (base URL, credentials) live in ConfigManager.
 */
public final class ApiEndpoints {

    private ApiEndpoints() {
        // Utility class — not instantiable
    }

    public static final String AUTH_PATH    = "/auth/login";
    public static final String BOOKING_PATH = "/booking";
    public static final String BOOKING_BY_ID_PATH = "/booking/{id}";
}