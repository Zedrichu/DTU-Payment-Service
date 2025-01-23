Feature: Reporting Service

  Scenario: Successful Payment History Updated
    Given a BankTransferConfirmed event is received with a payment record
    When the customer report is requested
    Then the customer report is received
    And the payment log is in the customer report
