Feature: dtupay.E2E DTUPay Reporting

  Scenario: Successful Reporting Generation for Customer
    Given a customer registered in DTUPay for reporting
    When the customer requests a report
    Then the customer report is retrieved

  Scenario: Successful Reporting Generation for Merchant
    Given a merchant registered in DTUPay for reporting
    When the merchant requests a report
    Then the merchant report is retrieved

  Scenario: Successful Reporting Generation for Manager
    When the manager requests a report
    Then the manager report is retrieved

  Scenario: Successful Reporting Generation After Transaction
    Given a registered customer with tokens
    And a registered merchant
    And the merchant has requested a payment for 100 from customer
    When the customer requests a report
    Then the customer report is retrieved
    And the customer report contains an entry
    When the manager requests a report
    Then the manager report is retrieved
    And the manager report contains an entry
    When the merchant requests a report
    Then the merchant report is retrieved
    And the merchant report contains an entry
