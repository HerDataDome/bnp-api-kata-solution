@regression
Feature: Create Booking - POST /booking
  As an API consumer
  I want to create hotel room bookings
  So that guests can reserve rooms with their details

  Background:
    Given the booking API is running

  # ── Happy Path ─────────────────────────────────────────────────────────────

  @smoke
  Scenario: C-01 Successfully create a booking with all valid fields
    When I create a booking with the following details:
      | roomid      | 6                        |
      | firstname   | John                     |
      | lastname    | Doe                      |
      | depositpaid | true                     |
      | checkin     | 2026-08-01               |
      | checkout    | 2026-08-03               |
      | email       | john.doe@example.com     |
      | phone       | 07911123456              |
    Then the response status code should be 201
    And the response should contain a numeric booking ID
    And the response booking details should match what was submitted

  @contract
  Scenario: C-02 Create booking response body matches the OpenAPI contract schema
    When I create a valid booking
    Then the response status code should be 201
    And the response body should match the booking creation contract schema
  
  @regression
  Scenario: C-03 Successfully create a booking when deposit is not paid
    When I create a booking with the following details:
      | roomid      | 3                        |
      | firstname   | Jane                     |
      | lastname    | Smith                    |
      | depositpaid | false                    |
      | checkin     | 2026-04-10               |
      | checkout    | 2026-04-14               |
      | email       | jane.smith@example.com   |
      | phone       | 07911123456              |
    Then the response status code should be 201
    And the response booking field "depositpaid" should be "false"

  # ── Date Logic Validation ───────────────────────────────────────────────────

  @negative
  Scenario: C-04 Reject a booking where checkout date is before checkin date
    When I create a booking with checkin "2026-06-10" and checkout "2026-06-05"
    Then the response status code should be 409
    And the error response should contain "Failed to create booking"

  @negative
  Scenario: C-05 Reject a booking where checkout date equals checkin date
    When I create a booking with checkin "2026-06-10" and checkout "2026-06-10"
    Then the response status code should be 409
    And the error response should contain "Failed to create booking"

  # ── Schema & Data Type Invalidity ───────────────────────────────────────────

  @negative @contract
  Scenario: C-06 Validation error response contains an errors array
    When I create a booking with firstname "Jo" and all other fields valid
    Then the response status code should be 400
    And the response should contain an "errors" field that is an array
    And the errors array should not be empty

  @negative
  Scenario: C-07 Document API behaviour when roomid is sent as a string instead of an integer
    When I create a booking with roomid as string value "two" and all other fields valid
    Then the response status code should be 400
    And the error response should contain "Failed to create booking"

  @negative
  Scenario: C-08 Document API behaviour when depositpaid is sent as an integer instead of a boolean
    When I create a booking with depositpaid as integer value 1 and all other fields valid
    Then the response status code should be 201
    And the response booking field "depositpaid" should be "true"

  @negative
  Scenario: C-09 Reject a booking when a required field is an empty string
    When I create a booking with roomid as empty string and all other fields valid
    Then the response status code should be 400
    And the error response should contain "must be greater than or equal to 1"

  # ── Data-Driven Boundary & Format Validations ──────────────────────────────

  @regression
  Scenario Outline: Successfully create bookings at exact boundary limits
    When I create a booking with "<field>" set to "<valid_value>" and all other fields valid
    Then the response status code should be 201
    And the response should contain a numeric booking ID

    Examples:
      | field     | valid_value                    |
      | firstname | Bob                            |
      | firstname | Bartholomew The Gr             |
      | lastname  | Doe                            |
      | lastname  | Bartholomew The Great And Migh |
      | phone     | 01234567890                    |
      | phone     | 012345678901234567890          |

  @negative
  Scenario Outline: Validation rejects bookings violating field constraints
    When I create a booking with "<field>" set to "<invalid_value>" and all other fields valid
    Then the response status code should be 400
    And the error response should contain "<expected_error>"

    Examples:
      | field     | invalid_value                          | expected_error                      |
      | firstname | Jo                                     | size must be between 3 and 18       |
      | firstname | Bartholomew Jameson                    | size must be between 3 and 18       |
      | lastname  | Li                                     | size must be between 3 and 30       |
      | lastname  | Bartholomew-Smithson-The-Third-Esquire | size must be between 3 and 30       |
      | phone     | 0123456789                             | size must be between 11 and 21      |
      | phone     | 0123456789012345678901                 | size must be between 11 and 21      |
      | email     | bad-email                              | must be a well-formed email address |
      | email     | userexample.com                        | must be a well-formed email address |