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
    Given a manager registered in DTUPay for reporting
    When the manager requests a report
    Then the manager report is retrieved