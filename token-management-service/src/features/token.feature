Feature: dtupay.services.token token management

  Scenario: Successful Token Generation
    When "TokensRequested" event is received for a token request
    When "TokenAccountVerified" event is received for a customer
    Then "TokensGenerated" event is sent with the same correlation id
    And then 3 valid tokens are generated

  Scenario: Successful Token Validation
    When "PaymentInitiated" event is received for a payment request
    Then "PaymentTokenVerified" is sent with the same correlation id

  Scenario: Successful Token Generation for Existing Customer
    When "TokensRequested" event is received for a token request for existing customer
    When "TokenAccountVerified" event is received for a customer
    Then "TokensGenerated" event is sent with the same correlation id



#  Scenario: Successful Token Generation for Existing Customer
#    When "TokensRequested" event is received for a token request
#    When "TokenAccountVerified" event is received for a customer
#    Then "TokensGenerated" event is sent with the same correlation id
