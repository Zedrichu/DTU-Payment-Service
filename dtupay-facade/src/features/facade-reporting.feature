Feature: dtupay.services.facade DTUPay Facade Reporting Feature
  Scenario: Successful Customer Report Request
    Given a customer that has performed no payments
    When a customer is requesting a customer report with a non existing customer id
    Then the "CustomerReportRequested" event is sent with an non existing customer id
    When the "CustomerReportGenerated" event is received for customer
    Then the retrieve report is empty



#  Scenario: Successful Merchant Report Request
#    Given a registered merchant and report of merchant
#    When a merchant report is requested with id
#    Then the "MerchantReportRequest" event for merchant is sent with their id
#    When the "MerchantReportGenerated" is received for the merchant id
#    Then a merchant report is retrieved


#  Scenario: Successful Manager Report Request
#    Given a registered manager and report of manager
#    When a manager report is requested with id
#    Then the "MerchantReportRequest" event for manager is sent with their id
#    When the "MerchantReportGenerated" is received for the manager id
#    Then a manager report is retrieved


