Feature: dtupay.services.account Payment Feature
  Scenario: Successful Merchant Validation
    Given a registered merchant
    When the "PaymentInitiated" event for the payment request is received
    Then the "MerchantAccountVerified" event is sent with the merchant information
    And the merchant account is verified

  Scenario: Successful Customer Validation
    Given a registered customer
    When the "PaymentTokenVerified" event for the customerid is received
    Then the "CustomerAccountVerified" event is sent with the customer information
    And the customer account is verified

