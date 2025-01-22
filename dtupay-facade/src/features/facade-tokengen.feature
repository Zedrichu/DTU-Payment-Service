Feature: dtupay.services.facade DTUPay Facade Token Generation Feature

  Scenario: Successful Token Request
    Given a registered customer
    When the customer requests 2 tokens
    Then the "TokensRequested" event is sent asking 2 tokens for that customer id
    When the "TokensGenerated" event is received for the same customer with 2 tokens
    Then the customer has 2 new valid tokens

  Scenario: Successful Double Token Request Interleaving
    Given a registered customer
    When the customer requests 3 tokens
    Then the "TokensRequested" event is sent asking 3 tokens for that customer id
    Given a second registered customer
    When the second customer requests 4 tokens
    Then the "TokensRequested" event is sent asking 4 tokens for the second customer id
    When the "TokensGenerated" event is received for the second customer with 4 tokens
    Then the second customer has 4 new valid tokens
    When the "TokensGenerated" event is received for the same customer with 3 tokens
    Then the customer has 3 new valid tokens

  Scenario: Unsuccessful Token Request Invalid Customer
    Given an unregistered customer id
    When the customer requests 4 tokens
    Then the "TokensRequested" event is sent asking 4 tokens for that customer id
    When the TokenGenerationFailed event is received for the same correlation id
    Then an InvalidAccount exception with message "No tokens generated: Invalid customer id." is raised

  Scenario: Unsuccessful Token Request Over Limit
    Given a registered customer
    When the customer requests 4 tokens
    Then the "TokensRequested" event is sent asking 4 tokens for that customer id
    When the TokenGenerationFailed event is received for the same correlation and customer id
    Then an InvalidAccount exception with message "No tokens generated: Too many tokens assigned." is raised