Feature: dtupay.E2E DTUPay Reporting

  Scenario: Successful Reporting Generation for Customer
    Given a customer registered in DTUPay for reporting
    When the customer requests a report
    Then the customer report is retrieved