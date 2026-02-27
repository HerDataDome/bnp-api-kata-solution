package com.booking.stepdefinitions;

import com.booking.config.ApiEndpoints;
import com.booking.config.ConfigManager;
import com.booking.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions shared across all feature files.
 */
public class CommonSteps {

    private final ScenarioContext context;

    public CommonSteps(ScenarioContext context) {
        this.context = context;
    }

    // ── Background ────────────────────────────────────────────────────────

    /**
     * Verifies the API is reachable before any scenario runs.
     * Hits GET /booking/actuator/health as defined in booking.yaml.
     * Fails fast with a clear message if the environment is down.
     */
    @Given("the booking API is running")
    public void theBookingApiIsRunning() {
        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .when()
                .get(ApiEndpoints.HEALTH_PATH);
        assertThat(response.getStatusCode())
                .as("API health check failed — is the environment reachable? Got status %d",
                        response.getStatusCode())
                .isEqualTo(200);
    }

    // ── Generic status code assertions ────────────────────────────────────

    /**
     * Parameterised status code assertion reused across all features.
     * Covers 200, 400, 401, 201 — any concrete status used in a feature file is matched by this single step at runtime.
     */
    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        assertThat(response.getStatusCode())
                .as("Expected HTTP %d but got %d.\nResponse body:\n%s",
                        expectedStatus,
                        response.getStatusCode(),
                        response.getBody().asPrettyString())
                .isEqualTo(expectedStatus);
    }

}