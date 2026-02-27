@regression
Feature: Delete Booking - DELETE /booking/{id}

  Background:
    Given the booking API is running
    And I have a valid authentication token
    And a booking exists in the system

  @smoke @bug
  Scenario: D-01 Successfully delete a booking and document 201 vs 200 contract violation
    When I cancel the booking with a valid auth token
    Then the response status code should be 200
    When I retrieve the booking by its ID
    Then the response status code should be 404

  @auth @negative
  Scenario Outline: D-02 Reject DELETE requests with missing or malformed auth cookie
    When I cancel the booking with auth token ""
    Then the response status code should be 401

  @auth @negative @bug
  Scenario: D-03 Document API crash (500) when token value is invalid but prefix is present
    When I cancel the booking with auth token "token=invalid_garbage"
    Then the response status code should be 500
    And the error response should contain "Failed to delete booking"