Feature: DTUPay Facade Feature

  # Jonas (s204713)
  Scenario: Successful Customer Registration
    Given a customer with name "Jonas", a CPR number "192323-2332", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreated" event is received with non-empty id
    Then the customer is registered and his id is set

  # Adrian (s204683)
  Scenario: Successful Double Customer Registration
    Given a customer with name "Jonas", a CPR number "213124-2231", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event for the customer is sent
    When the "CustomerAccountCreated" event is received with non-empty id
    Then the customer is registered and his id is set
    Given a second customer with name "Jeppe", a CPR number "213124-2232", a bank account and empty id
    When the second customer is being registered
    Then the "CustomerRegistrationRequested" event for the second customer is sent
    When the "CustomerAccountCreated" event is received with non-empty id
    Then the second customer is registered and his id is set
    And the customer IDs are different










