Feature: Payment
  Scenario: Successful Payment
    Given a user with name "Susan", last name "Baldwon", and CPR "030144-4421"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a customer with Simple DTU Pay using their bank account
    And a user with name "Daniel", last name "Oliversen", and CPR "131162-3045"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a merchant with Simple DTU Pay using their bank account
    When the merchant initiates a payment for 10 kr from the customer
    Then the payment is successful
    And the balance of the customer at the bank is 990 kr
    And the balance of the merchant at the bank is 1010 kr

  Scenario: List of Payments
    Given a user with name "Sussane", last name "Caroll", and CPR "030154-4499"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a customer with Simple DTU Pay using their bank account
    And a user with name "Daniel", last name "Oliverson", and CPR "131161-3049"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a merchant with Simple DTU Pay using their bank account
    Given a successful payment of 10 kr from the customer to the merchant
    When the manager asks for a list of payments
    Then the list contains a payment where customer "Sussane" paid 10 kr to merchant "Daniel"


  Scenario: Customer is not known
    Given a user with name "Daniel", last name "Oliver", and CPR "131161-1045"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a merchant with Simple DTU Pay using their bank account
    When the merchant initiates a payment for 10 kr using customer id "non-existent-id"
    Then the payment is not successful
    And an error message is returned saying "customer with id \"non-existent-id\" is unknown"


  Scenario: Merchant is not known
    Given a user with name "Daniel", last name "Oliver", and CPR "131161-9045"
    And the user is registered with the bank and an initial balance of 1000 kr
    And the user is registered as a customer with Simple DTU Pay using their bank account
    When the merchant initiates a payment for 10 kr using merchant id "non-existent-id"
    Then the payment is not successful
    And an error message is returned saying "merchant with id \"non-existent-id\" is unknown"


