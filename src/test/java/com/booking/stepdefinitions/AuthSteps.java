package com.booking.stepdefinitions;

import com.booking.clients.AuthClient;
import com.booking.config.ConfigManager;
import com.booking.context.ScenarioContext;
import com.booking.dto.TokenRequest;
import com.booking.dto.TokenResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {
    private final ScenarioContext context;
    private final AuthClient authClient;

    public AuthSteps(ScenarioContext context) {
        this.context = context;
        this.authClient = new AuthClient();
    }

    @When("I request a token with username {string} and password {string}")
    public void iRequestATokenWithUsernameAndPassword(String username, String password) {
        TokenRequest tokenRequest = TokenRequest.builder()
                .username(username)
                .password(password)
                .build();

        Response response = authClient.createToken(tokenRequest);
        context.set(ScenarioContext.ContextKey.LAST_RESPONSE, response);
    }

    @Then("the response should contain a non-empty token")
    public void theResponseShouldContainANonEmptyToken() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        TokenResponse tokenResponse = response.as(TokenResponse.class);
        assertThat(tokenResponse.getToken()).as("Expected a token in the response").isNotBlank();
    }

    @Given("I have a valid authentication token")
    public void iHaveAValidAuthenticationToken() {
        TokenRequest tokenRequest = TokenRequest.builder()
                .username(ConfigManager.getInstance().getAdminUsername())
                .password(ConfigManager.getInstance().getAdminPassword())
                .build();

        Response response = authClient.createToken(tokenRequest);
        assertThat(response.getStatusCode())
                .as("Background auth token generation failed").isEqualTo(200);

        TokenResponse tokenResponse = response.as(TokenResponse.class);
        String token = tokenResponse.getToken();
        assertThat(token).as("Auth token was blank").isNotBlank();
        
        // Centralize the Cookie prefix logic here
        context.set(ScenarioContext.ContextKey.AUTH_TOKEN, "token=" + token);
    }
}