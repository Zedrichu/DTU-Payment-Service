Feature: dtupay.services.account Account Deregistration Feature

  Scenario: Successful Customer Account Deregistration
    Given a customer stored in the account repository
    When a "CustomerDeregistrationRequested" event for the same customer id is received opting to deregister with a correlation id
    Then the "CustomerDeleted" event is sent with the same correlation id

  Scenario: Unsuccessful Customer Account Deregistration
    When a "CustomerDeregistrationRequested" event for a customer id is received opting to deregister with a correlation id
    Then the "CustomerDeleteFailed" event is sent with the same correlation id

  Scenario: Successful Merchant Account Deregistration
    Given a merchant stored in the account repository
    When a "MerchantDeregistrationRequested" event for the same merchant id is received opting to deregister with a correlation id
    Then the "MerchantDeleted" event is sent with the same correlation id

  Scenario: Unsuccessful Merchant Account Deregistration
    When a "MerchantDeregistrationRequested" event for a merchant id is received opting to deregister with a correlation id
    Then the "MerchantDeleteFailed" event is sent with the same correlation id

