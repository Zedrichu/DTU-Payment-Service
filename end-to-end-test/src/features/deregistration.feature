Feature: dtupay.E2E DTUPay Deregistration

  Scenario: Successful Deregistration Customer without tokens
     Given a customer registered in DTUPay
     When the customer is deregistered in DTUPay
     Then the customer is deregistered

  Scenario: Successful Deregistration Customer with tokens
    Given a customer registered in DTUPay
    And the customer has valid tokens
    When the customer is deregistered in DTUPay
    Then the customer is deregistered

  Scenario: Successful Deregistration Merchant
    Given a merchant registered in DTUPay
    When the merchant is deregistered in DTUPay
    Then the merchant is deregistered

  Scenario: Unsuccessful Deregistration of Customer
    Given a customer registered in DTUPay
    When the customer deregisters in DTUPay with the wrong ID
    Then the customer receives an error message "Customer Deregistration Failed"

  Scenario: Unsuccessful Deregistration of Merchant
    Given a merchant registered in DTUPay
    When the merchant deregisters in DTUPay with the wrong ID
    Then the merchant receives an error message "Merchant Deregistration Failed"