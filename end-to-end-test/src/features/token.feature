Feature: dtupay.E2E Token Generation

  Scenario: Successful Token Generation for New Customer
    Given a registered customer with DTUPay with 0 valid tokens
    When the customer requests 3 tokens
    Then the customer receives 3 tokens

  Scenario: Unsuccessful Zero Token Generation
    Given a registered customer with DTUPay with 1 valid tokens
    When the customer requests 0 tokens
    Then the token request is declined with TokenRequest exception and error message "No tokens generated: Invalid token amount."
    And the customer has a total of 1 valid tokens

  Scenario: Unsuccessful Negative Token Generation
    Given a registered customer with DTUPay with 1 valid tokens
    When the customer requests -3 tokens
    Then the token request is declined with TokenRequest exception and error message "No tokens generated: Invalid token amount."
    And the customer has a total of 1 valid tokens

  Scenario: Successful Token Generation
   Given a registered customer with DTUPay with 2 valid tokens
   When the customer requests 2 tokens
   Then the customer receives 2 tokens
   And  the customer has a total of 4 valid tokens

  Scenario: Unsuccessful Token Generation Number of Tokens
   Given a registered customer with DTUPay with 3 valid tokens
   When the customer requests 3 tokens
   Then the token request is declined with TokenRequest exception and error message "No tokens generated: Invalid token amount."
   And the customer has a total of 3 valid tokens

  Scenario: Unsuccessful Token Generation Unregistered Customer
    Given an unregistered customer with DTUPay with 3 valid tokens
    When the customer requests 2 tokens
    Then the token request is declined with TokenRequest exception and error message "No tokens generated: Invalid customer id."
    And the customer has a total of 3 valid tokens
