package com.booking.hooks;

import com.booking.clients.AuthClient;
import com.booking.clients.BookingClient;
import com.booking.config.ConfigManager;
import com.booking.context.ScenarioContext;
import com.booking.dto.TokenRequest;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.restassured.response.Response;

/**
 * Cucumber lifecycle hooks for scenario setup and teardown.
 *
 * Execution order:
 *   @Before(order = 0)  — setUp:              initialises Allure step
 *   @After(order = 20)  — attachLogsOnFailure: captures response/request to Allure (runs FIRST)
 *   @After(order = 10)  — teardownBookingData: deletes test data created during scenario (runs SECOND)
 *
 * Cucumber @After hooks execute in DESCENDING order — higher number runs first.
 * This guarantees logs are captured before data is deleted.
 *
 * ScenarioContext is injected by PicoContainer, giving each scenario its own isolated instance with no shared state between scenarios.
 *
 * BookingClient and AuthClient are instantiated directly (not injected) because
 * both are stateless — they hold only a RequestSpecification and carry no mutable state that could pollute between scenarios.
 */
public class Hooks {

    private final ScenarioContext context;
    private final BookingClient bookingClient;
    private final AuthClient authClient;

    public Hooks(ScenarioContext context) {
        this.context = context;
        this.bookingClient = new BookingClient();
        this.authClient = new AuthClient();
    }

    /**
     * Runs before every scenario.
     * Registers the scenario name as an Allure step for traceability.
     */
    @Before(order = 0)
    public void setUp(Scenario scenario) {
        Allure.step("Starting scenario: " + scenario.getName());
    }

    /**
     * Runs FIRST on teardown (order = 20, higher number = runs first in @After).
     * Attaches request and response details to the Allure report on failure,
     * providing full diagnostic context before any cleanup occurs.
     */
    @After(order = 20)
    public void attachLogsOnFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            if (context.contains(ScenarioContext.ContextKey.LAST_REQUEST_BODY)) {
                String requestBody = context.get(ScenarioContext.ContextKey.LAST_REQUEST_BODY);
                Allure.addAttachment("API Request Body", "application/json", requestBody);
            }
            if (context.contains(ScenarioContext.ContextKey.LAST_RESPONSE)) {
                Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
                Allure.addAttachment("API Status Code", "text/plain",
                        String.valueOf(response.getStatusCode()));
                Allure.addAttachment("API Response Body", "application/json",
                        response.getBody().asPrettyString());
            }
        }
    }

    /**
     * Runs SECOND on teardown (order = 10, lower number = runs second in @After).
     * Deletes any booking created during the scenario to prevent state pollution
     * between test runs.
     *
     * Teardown Logic: if the scenario under test did not authenticate
     * (e.g. a create-only happy path test), this hook dynamically fetches an admin token to perform the deletion,
     * ensuring cleanup always succeeds regardless of which scenarios ran before it.
     */
    @After(order = 10)
    public void teardownBookingData() {
        if (context.contains(ScenarioContext.ContextKey.BOOKING_ID)) {
            Object bookingId = context.get(ScenarioContext.ContextKey.BOOKING_ID);
            String tokenHeader = context.get(ScenarioContext.ContextKey.AUTH_TOKEN);

            if (tokenHeader == null) {
                TokenRequest creds = TokenRequest.builder()
                        .username(ConfigManager.getInstance().getAdminUsername())
                        .password(ConfigManager.getInstance().getAdminPassword())
                        .build();
                Response tokenResponse = authClient.createToken(creds);
                if (tokenResponse.getStatusCode() == 200) {
                    tokenHeader = "token=" + tokenResponse.jsonPath().getString("token");
                }
            }

            if (tokenHeader != null && bookingId != null) {
                bookingClient.deleteBooking(bookingId, tokenHeader);
                Allure.step("Teardown: deleted booking ID " + bookingId);
            }
        }
    }
}