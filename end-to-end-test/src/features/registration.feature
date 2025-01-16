Feature: DTUPay Registration

  Scenario: Successful Customer Registration
    Given a unregistered user with CPR "050505-0505" and name "Andrew" and lastname "Jones"
    And a registered bank account for the user with balance 1000
    When the user is registered as a customer in DTUPay
    Then the customer is registered with a non-empty customer id

#  Scenario: Two Customers receive different Ids
#    Given a unregistered user with CPR "050505-0506" and name "Susan" and lastname "Jones"
#    And a registered bank account for the user with balance 1000
#    And the user is registered as a customer in DTUPay
#    Given a unregistered user with CPR "050505-0505" and name "Andrew" and lastname "Jones"
#    And a registered bank account for the user with balance 1000
#    When the second user is registered as a customer in DTUPay
#    Then the customer IDs are different

  #  Scenario: Unsuccessful Customer Registration
#    Given a unregistered user with CPR "050505-0506" and name "John" and lastname "Doe"
#    And a user is registered as a customer in DTUPay
#    When the second user is registered as a customer in DTUPay
#    Then the user gets an error message

#  Scenario: Successful Merchant Registration
#   Given a unregistered user with CPR "050505-0505" and name "John" and lastname "Doe"
#   And a registered bank account for the user with balance 1000
#   When user is registered as a merchant in DTUPay
#   Then the merchant is registered with a non-empty customer id