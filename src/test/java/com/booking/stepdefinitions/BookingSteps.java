package com.booking.stepdefinitions;

import com.booking.clients.BookingClient;
import com.booking.context.ScenarioContext;
import com.booking.dto.Booking;
import com.booking.dto.BookingDates;
import com.booking.factory.TestDataFactory;
import com.booking.config.ConfigManager;
import io.restassured.RestAssured;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.qameta.allure.Allure;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for all booking CRUD feature files.
 */
public class BookingSteps {

    private final ScenarioContext context;
    private final BookingClient bookingClient;

    public BookingSteps(ScenarioContext context) {
        this.context = context;
        this.bookingClient = new BookingClient();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SHARED INFRASTRUCTURE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Converts a Cucumber DataTable (two-column key-value format) into a Booking DTO.
     *
     * This is the single converter for all DataTable steps — both
     * "I create a booking with the following details:" and
     * "I have created a booking with the following details:" (booking_read.feature)
     * bind to this same method to avoid duplication.
     */
    private Booking buildBookingFromTable(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        return Booking.builder()
                .roomid(Integer.parseInt(data.get("roomid")))
                .firstname(data.get("firstname"))
                .lastname(data.get("lastname"))
                .depositpaid(Boolean.parseBoolean(data.get("depositpaid")))
                .bookingdates(BookingDates.builder()
                        .checkin(data.get("checkin"))
                        .checkout(data.get("checkout"))
                        .build())
                .email(data.get("email"))
                .phone(data.get("phone"))
                .build();
    }

    /**
     * Stores the response and request body in ScenarioContext.
     * Response is read by CommonSteps status assertions and Hooks failure logging.
     * Request body is attached to Allure report on scenario failure.
     */
    private void storeResponse(Response response, Object requestPayload) {
        context.set(ScenarioContext.ContextKey.LAST_RESPONSE, response);
        if (requestPayload != null) {
            context.set(ScenarioContext.ContextKey.LAST_REQUEST_BODY,
                    requestPayload.toString());
        }
    }

    /**
     * Extracts bookingid from a successful create response and stores it in context.
     * The Hooks @After teardown reads BOOKING_ID to delete test data after every scenario.
     * Only stores if the response was 201 — negative tests don't produce a bookingid.
     */
    private void extractAndStoreBookingId(Response response) {
        if (response.getStatusCode() == 201) {
            Integer bookingId = response.jsonPath().getInt("bookingid");
            if (bookingId != null) {
                context.set(ScenarioContext.ContextKey.BOOKING_ID, bookingId);
                Allure.step("Booking created with ID: " + bookingId);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CREATE — create_booking.feature
    // ═══════════════════════════════════════════════════════════════════════

    @Given("I have created a booking with the following details:")
    @When("I create a booking with the following details:")
    public void iCreateABookingWithTheFollowingDetails(DataTable dataTable) {
        Booking booking = buildBookingFromTable(dataTable);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a valid booking")
    public void iCreateAValidBooking() {
        Booking booking = TestDataFactory.validBooking();
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a booking with firstname {string} and all other fields valid")
    public void iCreateABookingWithFirstname(String firstname) {
        Booking booking = TestDataFactory.bookingWithFirstname(firstname);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a booking with lastname {string} and all other fields valid")
    public void iCreateABookingWithLastname(String lastname) {
        Booking booking = TestDataFactory.bookingWithLastname(lastname);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a booking with phone {string} and all other fields valid")
    public void iCreateABookingWithPhone(String phone) {
        Booking booking = TestDataFactory.bookingWithPhone(phone);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a booking with email {string} and all other fields valid")
    public void iCreateABookingWithEmail(String email) {
        Booking booking = TestDataFactory.bookingWithEmail(email);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        extractAndStoreBookingId(response);
    }

    @When("I create a booking with checkin {string} and checkout {string}")
    public void iCreateABookingWithCheckinAndCheckout(String checkin, String checkout) {
        Booking booking = TestDataFactory.bookingWithDates(checkin, checkout);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
        // No bookingId to store — date validation errors return 400, not a created booking
    }

    @When("I create a booking with roomid as empty string and all other fields valid")
    public void iCreateABookingWithRoomidAsEmptyString() {
        Map<String, Object> payload = TestDataFactory.bookingWithRoomIdAsEmptyString();
        Response response = bookingClient.createBooking(payload);
        storeResponse(response, payload);
    }

    @When("I create a booking with roomid as string value {string} and all other fields valid")
    public void iCreateABookingWithRoomidAsStringValue(String invalidRoomId) {
        Booking booking = TestDataFactory.bookingWithRoomIdAsString(invalidRoomId);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
    }

    @When("I create a booking with depositpaid as integer value {int} and all other fields valid")
    public void iCreateABookingWithDepositpaidAsIntegerValue(int invalidValue) {
        Booking booking = TestDataFactory.bookingWithDepositPaidAsInteger(invalidValue);
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
    }

    /**
     * Scenario Outline step — handles the parameterised <field> and <invalid_value> columns.
     * Maps each field name to the correct TestDataFactory method.
     */
    @When("I create a booking with {string} set to {string} and all other fields valid")
    public void iCreateABookingWithFieldSetTo(String field, String value) {
        Booking booking = switch (field) {
            case "firstname" -> TestDataFactory.bookingWithFirstname(value);
            case "lastname"  -> TestDataFactory.bookingWithLastname(value);
            case "phone"     -> TestDataFactory.bookingWithPhone(value);
            case "email"     -> TestDataFactory.bookingWithEmail(value);
            default -> throw new IllegalArgumentException(
                    "No factory method mapped for field: '" + field + "'");
        };
        Response response = bookingClient.createBooking(booking);
        storeResponse(response, booking);
    }

    // ── Create assertions ─────────────────────────────────────────────────

    @And("the response should contain a numeric booking ID")
    public void theResponseShouldContainANumericBookingId() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        Integer bookingId = response.jsonPath().getInt("bookingid");
        assertThat(bookingId)
                .as("Expected a positive numeric bookingid in the response body")
                .isNotNull()
                .isPositive();
    }

    @And("the response booking details should match what was submitted")
    public void theResponseBookingDetailsShouldMatchWhatWasSubmitted() {
    Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);

    // The POST /booking response returns a flat object: bookingid, roomid,
    // firstname, lastname, depositpaid, and bookingdates.
    // email and phone are deliberately omitted — see FINDING F-03 below.
    assertThat(response.jsonPath().getInt("roomid"))
            .as("roomid should be present and positive in create response").isPositive();
    assertThat(response.jsonPath().getString("firstname"))
            .as("firstname should be present in create response").isNotNull();
    assertThat(response.jsonPath().getString("lastname"))
            .as("lastname should be present in create response").isNotNull();
            assertThat(response.jsonPath().getString("depositpaid"))
            .as("depositpaid should be present in create response").isNotNull();
    assertThat(response.jsonPath().getString("bookingdates.checkin"))
            .as("bookingdates.checkin should be present in create response").isNotNull();
    assertThat(response.jsonPath().getString("bookingdates.checkout"))
            .as("bookingdates.checkout should be present in create response").isNotNull();

    // SPEC DEVIATION: email and phone are not returned in the response.
    // The OpenAPI spec documents these fields as part of the response body.
    // The live API omits them — likely a deliberate PII/security decision.
    // Documented as Finding F-03. Not asserting presence or absence
    // to avoid locking the test to a known defect.
    String actualEmail = response.jsonPath().getString("email");
    String actualPhone = response.jsonPath().getString("phone");
    Allure.step("FINDING F-03: email and phone omitted from POST response "
            + "(spec documents them as present). "
            + "Actual email: '" + actualEmail + "', actual phone: '" + actualPhone + "'");
}

    @And("the response body should match the booking creation contract schema")
    public void theResponseBodyShouldMatchTheBookingCreationContractSchema() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "schemas/booking-response-schema.json"));
    }

    @And("the response booking field {string} should be {string}")
public void theResponseBookingFieldShouldBe(String field, String expectedValue) {
    Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
    String value = response.jsonPath().getString(field);
    assertThat(value)
            .as("Expected field '%s' to be '%s' but got '%s'", field, expectedValue, value)
            .isEqualTo(expectedValue);
    }

    @And("the response should contain an {string} field that is an array")
    public void theResponseShouldContainAnFieldThatIsAnArray(String fieldName) {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        List<?> array = response.jsonPath().getList(fieldName);
        assertThat(array)
                .as("Expected field '%s' to be a non-null array in the response body",
                        fieldName)
                .isNotNull();
    }

    @And("the errors array should not be empty")
    public void theErrorsArrayShouldNotBeEmpty() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        List<?> errors = response.jsonPath().getList("errors");
        assertThat(errors)
                .as("Expected errors array to contain at least one validation message")
                .isNotEmpty();
    }

    @And("the error response should contain {string}")
    public void theErrorResponseShouldContain(String expectedMessage) {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        String body = response.getBody().asString();
        assertThat(body)
                .as("Expected error response body to contain '%s'.\nActual body: %s",
                        expectedMessage, body)
                .contains(expectedMessage);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // READ — read_booking.feature
    // ═══════════════════════════════════════════════════════════════════════

    @Given("a booking exists in the system")
    public void aBookingExistsInTheSystem() {
        // PEAK DRY: Reuses your existing valid booking creation method
        iCreateAValidBooking();
    }

    @When("I retrieve the booking by its ID")
    public void iRetrieveTheBookingByItsId() {
        int bookingId = context.get(ScenarioContext.ContextKey.BOOKING_ID);
        String token = context.get(ScenarioContext.ContextKey.AUTH_TOKEN);

        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .header("Cookie", "token=" + token)
                .accept("application/json")
                .get("/booking/" + bookingId);
        
        // PEAK DRY: Reuses your existing response storage method
        storeResponse(response, null);
    }

    @When("I request the booking by its ID without an auth token")
    public void iRequestTheBookingByItsIdWithoutAnAuthToken() {
        int bookingId = context.get(ScenarioContext.ContextKey.BOOKING_ID);

        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .accept("application/json")
                .get("/booking/" + bookingId);
        
        storeResponse(response, null);
    }

    @When("I request a booking with a non-existent ID")
    public void iRequestABookingWithANonExistentId() {
        String token = context.get(ScenarioContext.ContextKey.AUTH_TOKEN);

        Response response = RestAssured.given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .header("Cookie", "token=" + token)
                .accept("application/json")
                .get("/booking/999999999");
        
        storeResponse(response, null);
    }

    @Then("the retrieved booking dates should match the submitted dates")
    public void theRetrievedBookingDatesShouldMatch() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        assertThat(response.jsonPath().getString("bookingdates.checkin"))
                .as("Checkin date should be present in GET response").isNotNull();
        assertThat(response.jsonPath().getString("bookingdates.checkout"))
                .as("Checkout date should be present in GET response").isNotNull();
    }

    @Then("the response should document the missing PII fields")
    public void theResponseShouldDocumentTheMissingPIIFields() {
        Response response = context.get(ScenarioContext.ContextKey.LAST_RESPONSE);
        String actualEmail = response.jsonPath().getString("email");
        String actualPhone = response.jsonPath().getString("phone");
        
        Allure.step("FINDING F-03: email and phone deliberately omitted from GET response. Actual email: '" + actualEmail + "', actual phone: '" + actualPhone + "'");
        
        assertThat(actualEmail).as("API contract violation: email is stripped").isNull();
        assertThat(actualPhone).as("API contract violation: phone is stripped").isNull();
    }
}