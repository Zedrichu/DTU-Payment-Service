Feature: Account Creation

  Scenario: Successful Customer Enrollment
    When a "CustomerRegistrationRequested" event for a customer is received
    Then the "CustomerAccountCreated" event is sent with the same correlation id
    And the customer account is assigned a customer id