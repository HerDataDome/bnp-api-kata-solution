package com.booking.clients;

import com.booking.config.ApiEndpoints;
import com.booking.config.ConfigManager;
import com.booking.dto.TokenRequest;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * HTTP client for the Authentication API (/auth/login).
 * Encapsulates the Rest-Assured RequestSpecification so step definitions contain zero HTTP boilerplate.
 * All methods return the full Rest-Assured Response object,
 * allowing step definitions to assert both success and failure scenarios without losing the response chain.
 */
public class AuthClient {

    private final RequestSpecification baseRequestSpec;

    public AuthClient() {
        this.baseRequestSpec = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType(ContentType.JSON)
                .log().all();
    }

    /**
     * Sends a POST request to generate an authentication token.
     * The @Step annotation surfaces this HTTP call as a named step in the Allure report.
     *
     * @param credentials The TokenRequest payload containing username and password.
     * @return The full Rest-Assured Response, enabling assertion of both
     *         200 (token present) and 401 (error message) scenarios.
     */
    @Step("POST /auth/login — authenticate and request token")
    public Response createToken(TokenRequest credentials) {
        return RestAssured.given(baseRequestSpec)
                .body(credentials)
                .when()
                .post(ApiEndpoints.AUTH_PATH);
    }
}