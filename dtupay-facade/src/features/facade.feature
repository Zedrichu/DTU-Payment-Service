Feature: DTUPay Facade Feature

  # Jonas (s204713)
  Scenario: Successful Customer Registration
    Given a customer with name "Jonas", a CPR number "192323-2332", a bank account and empty id
    When the customer is being registered
    Then the "CustomerRegistrationRequested" event is sent
    When the "CustomerRegistered" event is received with non-empty id
    Then the customer is registered and his id is set












