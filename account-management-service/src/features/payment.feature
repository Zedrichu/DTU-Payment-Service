Feature: dtupay.services.account Payment Feature
  Scenario: Successful Merchant Validation
    Given a registered merchant
    When the "PaymentInitiated" event for the payment request is received
    Then the "MerchantAccountVerified" event is sent with the merchant information
    And the merchant account is verified
