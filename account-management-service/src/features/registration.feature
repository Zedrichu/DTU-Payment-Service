Feature: dtupay.services.account Account Creation Feature

  Scenario: Successful Customer Enrollment
    When a "CustomerRegistrationRequested" event for a customer is received
    Then the "CustomerAccountCreated" event is sent with the same correlation id
    And the customer account is assigned a customer id

  Scenario: Unsuccessful Customer Enrollment (Bank Account Missing)
    When a "CustomerRegistrationRequested" event for a customer is received with missing bank account number
    Then the "CustomerAccountCreationFailed" event is sent with the same correlation id
    And the customer receives a failure message "Account creation failed: Provided customer must have a valid bank account number and CPR"

  Scenario: Successful Merchant Enrollment
    When a "MerchantRegistrationRequested" event for a merchant is received
    Then the "MerchantAccountCreated" event is sent with the same correlation id
    And the merchant account is assigned a merchant id

#    Author - Adrian Ursu (s240160)
  Scenario: Unsuccessful Merchant Enrollment (Bank Account Missing)
    When a "MerchantRegistrationRequested" event for a merchant is received with missing bank account
    Then the "MerchantAccountCreationFailed" event is sent with same correlation id
    And the merchant receives a failure message "Account creation failed: Provided merchant must have a valid bank account number and CPR"