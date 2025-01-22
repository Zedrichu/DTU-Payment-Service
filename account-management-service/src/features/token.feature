Feature: dtupay.services.account Token Generation Feature
  Scenario: Successful Customer Validation for Tokens
    Given a registered customer
    When the "TokensRequested" event for the customer id is received
    Then the "TokenAccountVerified" event is sent with no content and same correlation id

  Scenario: Unsuccessful Customer Validation for Tokens
    When the "TokensRequested" event for unknown customer id is received
    Then the "TokenAccountInvalid" event is sent with no content and same correlation id