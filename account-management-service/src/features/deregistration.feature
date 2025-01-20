Feature: dtupay.services.account Account Deregistration Feature

  Scenario: Successful Account Deregistration
    When a "CustomerRegistrationRequested" event for a customer is received
    Then the "CustomerAccountCreated" event is sent with the same correlation id
    And the customer account is assigned a customer id
