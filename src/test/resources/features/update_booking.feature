@regression
Feature: Update Booking - PUT and PATCH /booking/{id}

  Background:
    Given the booking API is running
    And I have a valid authentication token
    And a booking exists in the system

  @smoke
  Scenario: U-01 Successfully replace a booking using PUT and verify changes via GET
    When I update the booking using PUT with new details and roomid 99
    Then the response status code should be 200
    And the response should contain a success flag
    When I retrieve the booking by its ID
    Then the response status code should be 200
    And the response booking field "firstname" should be "UpdatedFirstName"
    And the response booking field "lastname" should be "UpdatedLastName"
    And the response should document the ignored roomid and missing PII fields

  @auth @negative
  Scenario: U-02 Reject PUT requests with malformed auth cookie (missing prefix)
    When I update the booking with auth token "invalid_garbage"
    Then the response status code should be 401
    And the error response should contain "Authentication required"

  @auth @negative
  Scenario: U-03 Reject PUT requests with invalid auth token value
    When I update the booking with auth token "token=invalid_garbage"
    Then the response status code should be 403
    And the error response should contain "Failed to update booking"

  @negative
  Scenario: U-04 Reject PUT requests for non-existent booking IDs
    When I update a non-existent booking using PUT with a valid auth token
    Then the response status code should be 404
    And the error response should contain "Failed to update booking"

  @bug
  Scenario: U-05 Document missing PATCH implementation
    When I send a partial update for the booking
    Then the response status code should be 405