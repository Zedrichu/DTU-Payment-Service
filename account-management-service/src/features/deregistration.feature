Feature: dtupay.services.account Account Deregistration Feature

  Scenario: Successful Account Deregistration
    Given a customer stored in the account repository
    When a "CustomerDeregistrationRequested" event for the same customer id is received opting to deregister with a correlation id
    Then the "CustomerDeleted" event is sent with the same correlation id

  Scenario: Unsuccessful Account Deregistration
    When a "CustomerDeregistrationRequested" event for a customer id is received opting to deregister with a correlation id
    Then the "CustomerDeleteFailed" event is sent with the same correlation id
