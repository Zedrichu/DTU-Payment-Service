Feature: dtupay.services.facade DTUPay Facade Payment Feature

  Scenario: Successful Payment Request
    Given a valid payment request
    When the payment request is initiated
    Then the "PaymentInitiated" event for the payment request is sent
    When the "BankTransferConfirmed" event is received
    Then the payment was successful

  Scenario: Successful Token Request
    Given a registered customer with 0 tokens
    When the customer requests 2 tokens
    Then the "TokensRequested" event is sent asking 2 tokens for that customer id
    When the "TokensGenerated" event is received for the same customer with 2 tokens
    Then the customer has 2 valid tokens

  Scenario: Unsuccessful Token Request Invalid Customer
    Given an unregistered customer id
    When the unregistered customer requests 4 tokens
    Then the "TokensRequested" event is sent asking 4 tokens for that customer id
    When the "TokenAccountInvalid" event is received
    Then an "InvalidAccount" exception with message "" is raised