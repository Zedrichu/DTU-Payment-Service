 Feature: dtupay.services.facade DTUPay Facade Deregistration Feature
  Scenario: Successful Customer Account Deregistration
    Given a registered customer with id opting to deregister
    When the customer is being deregistered
    Then the "CustomerDeregistrationRequested" event for the customer is sent with their id
    When the "CustomerDeleted" event is received for the customer id
    When the "CustomerTokensDeleted" event is received for the customer id
    Then the customer is deregistered

   Scenario: Unsuccessful Customer Account Deregistration
     Given a registered customer with id opting to deregister
     When the customer is being deregistered
     Then the "CustomerDeregistrationRequested" event for the customer is sent with their id
     When the "CustomerDeleteFailed" event is received for the customer id
     When the "CustomerTokensDeleted" event is received for the customer id
     Then the customer deregistration failed

   Scenario: Successful Merchant Account Deregistration
     Given a registered merchant with id opting to deregister
     When the merchant is being deregistered
     Then the "MerchantDeregistrationRequested" event for the merchant is sent with their id
     When the "MerchantDeleted" event is received for the merchant id
     Then the merchant is deregistered

   Scenario: Unsuccessful Merchant Account Deregistration
     Given a registered merchant with id opting to deregister
     When the merchant is being deregistered
     Then the "MerchantDeregistrationRequested" event for the merchant is sent with their id
     When the "MerchantDeleteFailed" event is received for the merchant id
     Then the merchant deregistration failed

