Feature: Reporting Service

  Scenario: Successful Payment History Updated
    Given a reporting service
    When a BankTransferConfirmed event is received
    Then the payment history is up