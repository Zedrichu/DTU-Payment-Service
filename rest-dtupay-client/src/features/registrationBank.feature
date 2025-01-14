Feature: Bank Registration

  Scenario: Successful Bank Account Creation
    Given a user with name "Allen", last name "Baldwin", and CPR "030184-4491"
    When the user is registered with the bank and an initial balance of 1000 kr
    Then the service returns a bank account number and no error

  Scenario: Successful Customer DTU Pay Registration
    Given a user with name "Susan", last name "Baldwon", and CPR "030154-4424"
    And the user is registered with the bank and an initial balance of 1000 kr
    When the user is registered as a customer with Simple DTU Pay using their bank account
    Then the customer is registered

  Scenario: Successful Merchant DTU Pay Registration
    Given a user with name "Daniel", last name "Baldwinning", and CPR "030154-4432"
    And the user is registered with the bank and an initial balance of 1000 kr
    When the user is registered as a merchant with Simple DTU Pay using their bank account
    Then the merchant is registered