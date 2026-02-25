package com.booking.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Thread-safe key-value store scoped to a single Cucumber scenario.
 * PicoContainer creates one fresh instance per scenario, so no state leaks between scenarios.
 * Keys are defined as an Enum to prevent typo-based key mismatches
 * (e.g. "bookingId" vs "booking_id") which cause silent test failures.
 * ConcurrentHashMap is used instead of HashMap to future-proof the framework for parallel test execution.
 */
public class ScenarioContext {

    public enum ContextKey {
        BOOKING_ID,
        AUTH_TOKEN,
        LAST_RESPONSE,
        LAST_REQUEST_BODY
    }

    private final Map<ContextKey, Object> data = new ConcurrentHashMap<>();

    public void set(ContextKey key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ContextKey key) {
        return (T) data.get(key);
    }

    public boolean contains(ContextKey key) {
        return data.containsKey(key);
    }

    public void clear() {
        data.clear();
    }
}