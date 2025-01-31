Feature: Reporting Service

  Scenario: Successful Customer Report Generation
    When a BankTransferConfirmed event is received
    Then the report projection is updated with a view for each role
    When the CustomerReportRequested event is received for an id
    Then the CustomerReportGenerated event is sent with same correlation id and views
    And the customer views contain the transaction received
    When the MerchantReportRequested event is received for an id
    Then the MerchantReportGenerated event is sent with same correlation id and views
    And the merchant views contain the transaction received
    When the ManagerReportRequested event is received for an id
    Then the ManagerReportGenerated event is sent with same correlation id and views
    And the manager views contain the transaction received