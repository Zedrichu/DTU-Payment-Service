Feature: dtupay.services.facade DTUPay Facade Payment Feature

  Scenario: Successful Payment Request
    Given a valid payment request
    When the payment request is initiated
    Then the "PaymentInitiated" event for the payment request is sent
    When the "BankTransferConfirmed" event is received
    Then the payment was successful
