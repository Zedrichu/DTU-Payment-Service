Feature: dtupay.services.facade DTUPay Facade Reporting Feature
  Scenario: Successful Customer Report Request
    Given a customer that has performed no payments
    When a customer is requesting a customer report with a non existing customer id
    Then the "CustomerReportRequested" event is sent with an non existing customer id
    When the "CustomerReportGenerated" event is received for customer
    Then the retrieved customer report is empty

  Scenario: Successful Merchant Report Request
    Given a merchant that has performed no payments
    When a merchant is requesting a merchant report with a non existing customer id
    Then the "MerchantReportRequested" event is sent with an non existing merchant id
    When the "MerchantReportGenerated" event is received for merchant
    Then the retrieved merchant report is empty

  Scenario: Successful Manager Report Request
    When a manager is requesting a manager report
    Then the "ManagerReportRequested" event is sent
    When the "ManagerReportGenerated" event is received for manager
    Then the retrieved manager report is empty


