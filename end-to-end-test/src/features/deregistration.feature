Feature: dtupay.E2E DTUPay Deregistration

  Scenario: Successful Deregistration Customer
     Given a customer registered in DTUPay
     When the customer is deregistered in DTUPay
     Then the customer receives a confirmation message "Customer Successful Deregistration"