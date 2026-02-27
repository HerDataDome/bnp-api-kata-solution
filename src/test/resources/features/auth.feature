@auth
Feature: Authentication - Token Generation

  Background:
    Given the booking API is running

  @smoke @regression
  Scenario: A-01 Successful token generation with valid credentials
    When I request a token with username "admin" and password "password"
    Then the response status code should be 200
    And the response should contain a non-empty token

  @negative
  Scenario Outline: A-02 Token generation fails with invalid or missing credentials
    When I request a token with username "<username>" and password "<password>"
    Then the response status code should be 401

    Examples:
      | username | password      |
      | admin    | wrongpassword |
      | baduser  | password      |
      |          |               |