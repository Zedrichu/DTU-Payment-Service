Feature: dtupay.E2E DTUPay Payment
    Scenario: Successful Payment
      Given a registered customer with DTUPay with tokens with balance 1000 in the bank
      And a registered merchant with DTUPay with balance 1000 in the bank
      When the merchant initiates a payment of 100
      Then the payment is successful
      And the customers balance in the bank is 900
      And the merchants balance in the bank is 1100

    Scenario: Unsuccessful Payment Not Sufficient Funds
      Given a registered customer with DTUPay with tokens with balance 0 in the bank
      And a registered merchant with DTUPay with balance 800 in the bank
      When the merchant initiates a payment of 100
      Then the payment is unsuccessful


#    Scenario: Unsuccessful Payment Unknown Merchant
#      Given a registered customer with DTUPay with tokens with balance 0 in the bank
#      And an unregistered merchant with DTUPay
#      When the merchant initiates a payment of 100
#      Then the payment is unsuccessful
