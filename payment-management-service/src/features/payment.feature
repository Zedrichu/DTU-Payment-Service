  Feature: dtupay.services.payment Payment Execution

  Scenario: Successful Transfer of Money
    When the "PaymentInitiated" event for a request is received
    When the "CustomerAccountVerified" event for a customer is received
    When the "MerchantAccountVerified" event for a merchant is received
    Then the "BankTransferConfirmed" event is sent with the same correlation id
    And the amount in the payment request has been debited from the customer's bank account


  Scenario: Unsuccessful Transfer of Money from invalid Customer
    When the "PaymentInitiated" event for a request is received
    When the "CustomerAccountInvalid" event for an error is received
    And the "MerchantAccountVerified" event for a merchant is received
    Then the "BankTransferFailed" event is sent with the same correlation id

  Scenario: Unsuccessful Transfer of Money from invalid Merchant
    When the "PaymentInitiated" event for a request is received
    When the "CustomerAccountVerified" event for a customer is received
    And the "MerchantAccountInvalid" event for an error is received
    Then the "BankTransferFailed" event is sent with the same correlation id

  # Paul Becker (s194702)
  Scenario: Unsuccessful Transfer of Money due to invalid token
    When the "MerchantAccountInvalid" event for an error is received
    When the "PaymentInitiated" event for a request is received
    And the "CustomerAccountVerified" event for a customer is received
    Then the "BankTransferFailed" event is sent with the same correlation id

  Scenario: Unsuccessful Transfer of Money due to invalid token
    When the "PaymentTokenInvalid" event for an error is received
    When the "PaymentInitiated" event for a request is received
    And the "MerchantAccountVerified" event for a merchant is received
    Then the "BankTransferFailed" event is sent with the same correlation id

#    Author - Adrian Ursu (s240160)
  Scenario: Unsuccessful Transfer of Money due to insufficient funds
    When the "PaymentInitiated" event for a request over the balance is received
    When the "CustomerAccountVerified" event for a customer is received
    And the "MerchantAccountVerified" event for a merchant is received
    When the "BankTransferFailed" event is sent with the same correlation id