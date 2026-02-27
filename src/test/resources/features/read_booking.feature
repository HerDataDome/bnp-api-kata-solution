@regression
Feature: Read Booking - GET /booking/{id}

  Background:
    Given the booking API is running
    And I have a valid authentication token

  @smoke
  Scenario: R-01 Successfully retrieve a booking and verify exact data persistence
    Given I have created a booking with the following details:
      | roomid      | 120                      |
      | firstname   | Alice                    |
      | lastname    | Cooper                   |
      | depositpaid | true                     |
      | checkin     | 2026-08-01               |
      | checkout    | 2026-08-07               |
      | email       | alice.cooper@example.com |
      | phone       | 01234567892              |
    When I retrieve the booking by its ID
    Then the response status code should be 200
    And the response booking field "firstname" should be "Alice"
    And the response booking field "lastname" should be "Cooper"
    And the response booking field "depositpaid" should be "true"
    And the retrieved booking dates should match the submitted dates
    And the response should document the missing PII fields

  @negative
  Scenario: R-02 Reject a retrieval request made without an auth token
    Given a booking exists in the system
    When I request the booking by its ID without an auth token
    Then the response status code should be 401

  @negative
  Scenario: R-03 Returns 404 when requesting a booking ID that does not exist
    When I request a booking with a non-existent ID
    Then the response status code should be 404