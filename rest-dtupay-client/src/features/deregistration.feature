Feature: Deregistration

  Scenario: Deregister customer
    Given a customer with name "Susan"
    And the customer is registered with Simple DTU Pay
    Given a merchant with name "Daniel"
    And the merchant is registered with Simple DTU Pay
    When the customer is deregistered
    And the merchant initiates a payment for 10 kr using obtained customer id
    Then the payment is not successful with error message containing "customer with id <id> is unknown"

  Scenario: Deregister merchant
    Given a customer with name "Susan"
    And the customer is registered with Simple DTU Pay
    Given a merchant with name "Daniel"
    And the merchant is registered with Simple DTU Pay
    When the merchant is deregistered
    And the merchant initiates a payment for 10 kr using obtained merchant id
    Then the payment is not successful with error message containing "merchant with id <id> is unknown"