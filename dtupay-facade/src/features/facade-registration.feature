Feature: dtupay.services.facade DTUPay Facade Registration Feature

  # Jonas (s204713)
  Scenario: Successful Customer Registration
    Given a customer with name "Jonas", a CPR number "192323-2332", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreated" event is received for customer with non-empty id
    Then the customer is registered and his id is set

    # Adrian (s204683)
  Scenario: Successful Double Customer Registration (interleaving)
    Given a customer with name "Jonas", a CPR number "213124-2231", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    Given a second customer with name "Jeppe", a CPR number "213124-2232", a bank account and empty id
    When the second customer is being registered
    Then the "CustomerRegistrationRequested" event for the second customer is sent
    When the "CustomerAccountCreated" event is received for second customer with non-empty id
    Then the second customer is registered and his id is set
    When the "CustomerAccountCreated" event is received for customer with non-empty id
    Then the customer is registered and his id is set
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

  Scenario: Unsuccessful Merchant Registration
    Given a merchant with name "Tom", a CPR number "213124-1234", no bank account and empty id
    When the merchant is being registered
    Then the "MerchantRegistrationRequested" event for the merchant is sent
    When the "MerchantAccountCreationFailed" event is received for the merchant
    Then an exception raises with the merchant declined error message "Account creation failed: Provided merchant must have a valid bank account number and CPR"

