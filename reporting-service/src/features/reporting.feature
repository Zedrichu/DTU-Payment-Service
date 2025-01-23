Feature: Reporting Service
# We made the design choice that no matter what id is provided even though it is not in the database, the response will be returned with an empty payment log.
# Hence all scenarios are successful.
  Scenario: Successful Payment History Updated
    Given a BankTransferConfirmed event is received with a payment record
    When the customer report is requested
    Then the customer report is received
    And the payment log is in the customer report

  Scenario: Successful Payment History with No Entries Id Customer
    When the customer report is requested with for non-existing id
    Then the customer report is received
    And the payment log is empty in the customer report

  Scenario: Successful Payment History with Merchant
    Given a BankTransferConfirmed event is received with a payment record
    When the merchant report is requested
    Then the merchant report is received
    And the payment log is in the merchant report

  Scenario: Successful Payment History with No Entries Id Merchant
    When the merchant report is requested with for non-existing id
    Then the merchant report is received
    And the payment log is empty in the merchant report

  Scenario: Successful Payment History with Manager
   Given a BankTransferConfirmed event is received with a payment record
   When the manager report is requested
   Then the manager report is received
   And the payment log is in the manager report

  Scenario: Successful Manager Payment History with No Entries Manager
    When the manager report is requested with no entries
    Then the manager report is received
    And the payment log is empty in the manager report
