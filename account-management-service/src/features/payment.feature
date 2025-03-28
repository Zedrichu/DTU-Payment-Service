Feature: dtupay.services.account Payment Feature
  Scenario: Successful Merchant Validation
    Given a registered merchant
    When the "PaymentInitiated" event for the payment request is received
    Then the "MerchantAccountVerified" event is sent with the merchant information
    And the merchant account is verified

  Scenario: Unsuccessful Merchant Validation
    Given an unregistered merchant
    When the "PaymentInitiated" event for the payment request is received
    Then the "MerchantAccountInvalid" event is sent with the error message "Merchant not registered."


    # Author: Paul Becker (s194702)
  Scenario: Successful Customer Validation
    Given a registered customer
    When the "PaymentTokenVerified" event for the customer id is received
    Then the "CustomerAccountVerified" event is sent with the customer information
    And the customer account is verified

  Scenario: Unsuccessful Customer Validation
    Given an unregistered customer
    When the "PaymentTokenVerified" event for the customer id is received
    Then the "CustomerAccountInvalid" event is sent with the error message "Customer not registered."





#  Scenario: Unsuccessful Customer Registration
#    Given an unregistered user with CPR "050505-0506" and name "John" and lastname "Doe"
#    And the user does not have a bank account
#    When the user is registered as a customer in DTUPay
#    Then the user gets an error message "Account creation failed: Provided customer must have a valid bank account number and CPR" and is not registered