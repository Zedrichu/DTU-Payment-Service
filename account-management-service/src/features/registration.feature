Feature: Account Creation

  Scenario: Successful Customer Enrollment
    When a "CustomerRegistrationRequested" event for a customer is received
    Then the customer gets an id