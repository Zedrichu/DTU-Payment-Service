# Feature: dtupay.services.facade DTUPay Facade Deregistration Feature
#  Scenario: Successful Customer Account Deregistration
#    Given a registered customer with id opting to deregister
#    When the customer is being deregistered
#    Then the "CustomerDeregistrationRequested" event for the customer is sent with their id
#    When the "CustomerDeregistered" event is received for the customer id
#    Then the customer is deregistered