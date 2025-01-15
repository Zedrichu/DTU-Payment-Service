Feature: DTUPay Registration

  Scenario: Successful Customer Registration
    Given a user with CPR "050505-0505" and name "Andrew" and lastname "Jones"
    And a registered bank account for the user with balance 1000
    When the user is registered as a customer in DTUPay
    Then the customer is registered with a customer id
