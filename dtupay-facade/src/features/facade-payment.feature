Feature: dtupay.services.facade DTUPay Facade Payment Feature

  Scenario: Successful Payment Request
    Given a payment request
    When the payment request is initiated
    Then the "PaymentInitiated" event for the payment request is sent
    When the BankTransferConfirmed event is received for the same correlation id
    Then the payment was successful

  Scenario: Unsuccessful Payment Request
    Given a payment request
    When the payment request is initiated
    Then the "PaymentInitiated" event for the payment request is sent
    When the BankTransferFailed event is received for the same correlation id
    Then an BankFailure exception with message "Bank transfer failed" is raised
