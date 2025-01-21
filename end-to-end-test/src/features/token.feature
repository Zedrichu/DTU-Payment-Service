Feature: dtupay.E2E Token Generation
#
  Scenario:
    Given a registered customer with DTUPay without valid tokens
    When the customer requests 3 tokens
    Then the customer receives 3 tokens