package com.booking.stepdefinitions;

import com.booking.config.ConfigManager;
import com.booking.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {
    private final ScenarioContext context;

    public AuthSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("I request a token with username {string} and password {string}")
    public void iRequestATokenWithUsernameAndPassword(String username, String password) {
        Map<String, String> credentials = Map.of("username", username, "password", password);
        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/auth/login");
        context.set(ScenarioContext.ContextKey.LAST_RESPONSE, response);
    }

    @Then("the response should contain a non-empty token")
    public void theResponseShouldContainANonEmptyToken() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        String token = response.jsonPath().getString("token");
        assertThat(token).as("Expected a token in the response").isNotBlank();
    }

    @Given("I have a valid authentication token")
    public void iHaveAValidAuthenticationToken() {
        Map<String, String> credentials = Map.of("username", "admin", "password", "password");
        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/auth/login");
        String token = response.jsonPath().getString("token");
        assertThat(token).as("Failed to generate background auth token").isNotBlank();
        context.set(ScenarioContext.ContextKey.AUTH_TOKEN, token);
    }
}