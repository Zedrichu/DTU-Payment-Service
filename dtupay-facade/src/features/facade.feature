Feature: dtupay.services.facade DTUPay Facade Feature

  # Jonas (s204713)
  Scenario: Successful Customer Registration
    Given a customer with name "Jonas", a CPR number "192323-2332", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreated" event is received for customer with non-empty id
    Then the customer is registered and his id is set

  # Adrian (s204683)
  Scenario: Successful Double Customer Registration
    Given a customer with name "Jonas", a CPR number "213124-2231", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreated" event is received for customer with non-empty id
    Then the customer is registered and his id is set
    Given a second customer with name "Jeppe", a CPR number "213124-2232", a bank account and empty id
    When the second customer is being registered
    Then the "CustomerRegistrationRequested" event for the second customer is sent
    When the "CustomerAccountCreated" event is received for second customer with non-empty id
    Then the second customer is registered and his id is set
    And the customer IDs are different

  Scenario: Unsuccessful Customer Registration
    Given a customer with name "Tom", a CPR number "213124-1234", no bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreationFailed" event is received for the customer
    Then an exception raises with error message "Account creation failed: Provided customer must have a valid bank account number and CPR"

  Scenario: Successful Merchant Registration
    Given a merchant with name "Simon", a CPR number "111111-1111", a bank account and empty id
    When the merchant is being registered
    Then the "MerchantRegistrationRequested" event for the merchant is sent
    When the "MerchantAccountCreated" event is received for merchant with non-empty id
    Then the merchant is registered and their id is set

  Scenario: Successful Customer Account Deregistration
    Given a registered customer with id opting to deregister
    When the customer is being deregistered
    Then the "CustomerDeregistrationRequested" event for the customer is sent with their id
    When the "CustomerDeregistered" event is received for the customer id
    Then the customer is deregistered

  Scenario: Successful Payment
    Given a valid payment request
    When the payment request is initiated
    Then the "PaymentInitiated" event for the payment request is sent
    When the "BankTransferConfirmed" event is received
    Then the payment was successful

  Scenario: Successful Token Request
    Given a registered customer with 0 tokens
    When the customer requests 2 tokens
    Then the "TokensRequested" event is sent asking 2 tokens for that customer id
    When the "TokensGenerated" event is received for the same customer with 2 tokens
    Then the customer has 2 valid tokens

#  Scenario: Unsuccessful Customer Registration
#    Given an unregistered user with CPR "050505-0506" and name "John" and lastname "Doe"
#    And the user does not have a bank account
#    When the user is registered as a customer in DTUPay
#    Then the user gets an error message "Account creation failed: Provided customer must have a valid bank account number and CPR" and is not registered