Feature: Reporting Service

  Scenario: Successful Payment History Updated
    Given a BankTransferConfirmed event is received with a payment record
    When the customer report is requested
    Then the customer report is received
    And the payment log is in the customer report

  Scenario: Successful Payment History with No Entries
    When the customer report is requested with for non-existing id
    Then the customer report is received
    And the payment log is empty in the customer report

  Scenario: Successful Payment History with Merchant
    Given a BankTransferConfirmed event is received with a payment record
    When the merchant report is requested
    Then the merchant report is received
    And the payment log is in the merchant report
