Feature: dtupay.E2E DTUPay Registration

  Scenario: Successful Customer Registration
    Given an unregistered user with CPR "050505-0505" and name "Andrew" and lastname "Jones"
    And a registered bank account for the user with balance 1000
    When the user is registered as a customer in DTUPay
    Then the customer is registered with a non-empty customer id

  Scenario: Two Customers receive different Ids
    Given an unregistered user with CPR "050505-0506" and name "Susan" and lastname "Jones"
    And a registered bank account for the user with balance 1000
    And the user is registered as a customer in DTUPay
    Given an unregistered user with CPR "050505-0505" and name "Andrew" and lastname "Jones"
    And a registered bank account for the user with balance 1000
    When the second user is registered as a customer in DTUPay
    Then the customer IDs are different

  ### Adrian Zvizdenco (s204683)
  Scenario: Unsuccessful Customer Registration No Bank
    Given an unregistered user with CPR "050505-0506" and name "John" and lastname "Doe"
    And the user does not have a bank account
    When the user is registered as a customer in DTUPay
    Then the user gets an error message "Account creation failed: Provided customer must have a valid bank account number and CPR" and is not registered

  Scenario: Unsuccessful Merchant Registration No Bank
    Given an unregistered user with CPR "050505-0116" and name "John" and lastname "Doe"
    And the user does not have a bank account
    When the user is registered as a merchant in DTUPay
    Then the user gets an error message "Account creation failed: Provided merchant must have a valid bank account number and CPR" and is not registered

  Scenario: Successful Merchant Registration
   Given an unregistered user with CPR "050505-0505" and name "John" and lastname "Doe"
   And a registered bank account for the user with balance 1000
   When user is registered as a merchant in DTUPay
   Then the merchant is registered with a non-empty merchant id