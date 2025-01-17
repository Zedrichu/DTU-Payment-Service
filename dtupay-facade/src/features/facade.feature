Feature: DTUPay Facade Feature

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


#  Scenario: Unsuccessful Customer Registration
#    Given an unregistered user with CPR "050505-0506" and name "John" and lastname "Doe"
#    And the user does not have a bank account
#    When the user is registered as a customer in DTUPay
#    Then the user gets an error message "Account creation failed: Provided customer must have a valid bank account number and CPR" and is not registered