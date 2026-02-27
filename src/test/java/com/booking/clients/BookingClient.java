package com.booking.clients;

import com.booking.config.ApiEndpoints;
import com.booking.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * HTTP client for the Booking API (/booking and /booking/{id}).
 * Encapsulates the Rest-Assured RequestSpecification so step definitions contain zero HTTP boilerplate.
 *
 * Payload parameters are typed as Object (not Booking DTO) intentionally:
 * negative tests require sending malformed types
 * (e.g. a String for roomid, an Integer for depositpaid).
 * A typed Booking parameter would make those tests impossible to write.
 *
 * All methods return the full Rest-Assured Response object, allowing step definitions to assert both success and failure scenarios
 * without losing the response chain.
 *
 * Note: BookingClient and AuthClient are intentionally instantiated with
 * new() in Hooks rather than injected via PicoContainer. Both clients are
 * stateless (they hold only a RequestSpecification) so separate instances
 * carry no risk of state pollution between scenarios.
 */
public class BookingClient {

    private final RequestSpecification baseRequestSpec;

    public BookingClient() {
        this.baseRequestSpec = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType(ContentType.JSON)
                .log().all();
    }

    /**
     * POST /booking — creates a new booking.
     *
     * @param payload The request body. Pass a Booking DTO for happy-path tests,
     *                or a raw Map/String for negative type-validation tests.
     * @return The full Rest-Assured Response.
     */
    public Response createBooking(Object payload) {
        return RestAssured.given(baseRequestSpec)
                .body(payload)
                .when()
                .post(ApiEndpoints.BOOKING_PATH);
    }

    /**
     * GET /booking/{id} — retrieves a booking by ID.
     *
     * @param id           The booking ID. Typed as Object to support edge-case inputs.
     * @param cookieHeader The full Cookie header string (e.g. "token=abc123").
     *                     Pass null to omit the header — tests unauthorized access (401).
     * @return The full Rest-Assured Response.
     */
    public Response getBooking(Object id, String cookieHeader) {
        RequestSpecification req = RestAssured.given(baseRequestSpec)
                .pathParam("id", id);
        if (cookieHeader != null) {
            req.header("Cookie", cookieHeader);
        }
        return req.when().get(ApiEndpoints.BOOKING_BY_ID_PATH);
    }

    /**
     * PUT /booking/{id} — fully replaces a booking.
     *
     * @param id           The booking ID.
     * @param payload      The full replacement booking body.
     * @param cookieHeader Cookie header string. Pass null to test unauthorized access.
     * @return The full Rest-Assured Response.
     */
    public Response updateBooking(Object id, Object payload, String cookieHeader) {
        RequestSpecification req = RestAssured.given(baseRequestSpec)
                .pathParam("id", id);
        if (cookieHeader != null) {
            req.header("Cookie", cookieHeader);
        }
        return req.body(payload).when().put(ApiEndpoints.BOOKING_BY_ID_PATH);
    }

    /**
     * PATCH /booking/{id} — partially updates a booking.
     * Only fields present in the payload are updated; absent fields retain their values.
     *
     * @param id           The booking ID.
     * @param payload      Partial body containing only the fields to update.
     * @param cookieHeader Cookie header string. Pass null to test unauthorized access.
     * @return The full Rest-Assured Response.
     */
    public Response partialUpdateBooking(Object id, Object payload, String cookieHeader) {
        RequestSpecification req = RestAssured.given(baseRequestSpec)
                .pathParam("id", id);
        if (cookieHeader != null) {
            req.header("Cookie", cookieHeader);
        }
        return req.body(payload).when().patch(ApiEndpoints.BOOKING_BY_ID_PATH);
    }

    /**
     * DELETE /booking/{id} — deletes a booking.
     * Note: the API returns 201 (not 200) on successful deletion per the OpenAPI spec.
     *
     * @param id           The booking ID.
     * @param cookieHeader Cookie header string. Pass null to test unauthorized access.
     * @return The full Rest-Assured Response.
     */
    public Response deleteBooking(Object id, String cookieHeader) {
        RequestSpecification req = RestAssured.given(baseRequestSpec)
                .pathParam("id", id);
        if (cookieHeader != null) {
            req.header("Cookie", cookieHeader);
        }
        return req.when().delete(ApiEndpoints.BOOKING_BY_ID_PATH);
    }
}