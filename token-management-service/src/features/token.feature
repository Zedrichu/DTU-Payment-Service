Feature: dtupay.services.token token management

  # Jonas Kjeldsen (s204713) and Jeppe Mikkelsen (s)
  Scenario: Successful Payment Token Validation
    Given an existing customer with 1 tokens assigned
    When PaymentInitiated event is received for a payment request
    Then PaymentTokenVerified event is sent for the payment with the customer id and the same correlation id
    Then the token is no longer valid

  # Paul Becker (s194702)
  Scenario: Unsuccessful Payment Token Validation
    Given an existing customer with 1 tokens assigned
    When PaymentInitiated event is received for a payment request with an invalid token
    Then PaymentTokenInvalid event is sent with error "Invalid token." with the same correlation id

  # Adrian Zvizdenco (s204683)
  Scenario: Successful Token Generation for New Customer
    When TokensRequested event is received for 3 tokens
    When TokenAccountVerified event is received for a customer
    Then TokensGenerated event is sent with the same correlation id
    And 3 valid tokens are generated
    And the customer has 3 valid tokens

  # Adrian Zvizdenco (s204683)
  Scenario: Successful Token Generation for Existing Customer
    Given an existing customer with 1 tokens assigned
    When TokensRequested event is received for 3 tokens for the same customer id
    When TokenAccountVerified event is received for a customer
    Then TokensGenerated event is sent with the same correlation id
    And 3 valid tokens are generated
    And the customer has 4 valid tokens

  Scenario: Unsuccessful Token Generation Token Limit
    Given an existing customer with 3 tokens assigned
    When TokensRequested event is received for 3 tokens for the same customer id
    When TokenAccountVerified event is received for a customer
    Then TokensGenerationFailed event is sent with the same correlation id
    And error message "No tokens generated: Invalid token amount."
    And the customer has 3 valid tokens

  Scenario: Unsuccessful Token Generation Invalid Account
    Given an existing customer with 1 tokens assigned
    When TokensRequested event is received for 3 tokens for the same customer id
    When TokenAccountInvalid event is received for a customer
    Then TokensGenerationFailed event is sent with the same correlation id
    And error message "No tokens generated: Invalid customer id."
    And the customer has 1 valid tokens

  Scenario: Successful Deregistration of Tokens
    Given an existing customer with 5 tokens assigned
    When CustomerDeregistrationRequested event is received for the same customer id
    Then CustomerTokensDeleted event is sent with the same correlation id
    And there is exist no customer in token memory repository

  Scenario: Successful Deregistration of Token with No Customer Id
    When CustomerDeregistrationRequested event is received for a customer id
    Then CustomerTokensDeleted event is sent with the same correlation id
    And there is exist no customer in token memory repository