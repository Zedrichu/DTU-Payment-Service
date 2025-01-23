package tests.service.behaviours;

import com.google.gson.Gson;
import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.Report;
import dtupay.services.reporting.domain.models.Token;
import dtupay.services.reporting.domain.models.views.CustomerView;
import dtupay.services.reporting.domain.models.views.ManagerView;
import dtupay.services.reporting.domain.models.views.MerchantView;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import messaging.Event;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReportingStepDefs {

    MessageQueue queue = mock(MessageQueue.class);
    private ReportingManager reportingManager = new ReportingManager(queue);
    PaymentRecord paymentRecord;
    Correlator correlator = Correlator.random();
    Report<CustomerView> customerReport;
    Report<MerchantView> merchantReport;
    Report<ManagerView> managerReport;


  @Given("a BankTransferConfirmed event is received with a payment record")
  public void aBankTransferConfirmedEventIsReceivedWithAPaymentRecord() {
    EventTypes eventType = EventTypes.BANK_TRANSFER_CONFIRMED;
    paymentRecord = new PaymentRecord(
          "cBankAccount",
          "mBankAccount",
          1000,
          "Payment happened",
          Token.random(), // TODO: replace with updated token
          "cId",
          "mId"
    );
    reportingManager.handleBankTransferConfirmed(new Event(eventType.getTopic(),new Object[] {paymentRecord, Correlator.random()}));
  }

  @When("the customer report is requested")
  public void theCustomerReportIsRequested() {
    EventTypes eventType = EventTypes.CUSTOMER_REPORT_REQUESTED;

    reportingManager.handleCustomerReportRequested(new Event(eventType.getTopic(), new Object[] {paymentRecord.customerId(), correlator}));
  }



  @Then("the customer report is received")
  public void theCustomerReportIsReceived() {
    EventTypes eventType = EventTypes.CUSTOMER_REPORT_GENERATED;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(queue).publish(eventCaptor.capture());
    Event receivedEvent = eventCaptor.getValue();
    customerReport = receivedEvent.getArgument(0,
          new Report<CustomerView>(new ArrayList<>()) {}.getClass().getGenericSuperclass());

    assertEquals(eventType.getTopic(), receivedEvent.getTopic());
    assertEquals(correlator, receivedEvent.getArgument(1, Correlator.class));
  }

  @And("the payment log is in the customer report")
  public void thePaymentLogIsInTheCustomerReport(){
    CustomerView customerView = new CustomerView(paymentRecord.merchantId(),paymentRecord.token(),paymentRecord.amount());
    assertTrue(customerReport.getEntries().contains(customerView));

  }

  @When("the customer report is requested with for non-existing id")
  public void theCustomerReportIsRequestedWithForNonExistingId() {
    EventTypes eventType = EventTypes.CUSTOMER_REPORT_REQUESTED;
    reportingManager.handleCustomerReportRequested(new Event(eventType.getTopic(), new Object[] {"cId", correlator}));

  }

  @And("the payment log is empty in the customer report")
  public void thePaymentLogIsEmptyInTheCustomerReport() {
    assertTrue(customerReport.getEntries().isEmpty());
  }

  @When("the merchant report is requested")
  public void theMerchantReportIsRequested() {
    EventTypes eventType = EventTypes.MERCHANT_REPORT_REQUESTED;
    reportingManager.handleMerchantReportRequested(new Event(eventType.getTopic(), new Object[] {paymentRecord.merchantId(), correlator}));
  }

  @Then("the merchant report is received")
  public void theMerchantReportIsReceived() {
    EventTypes eventType = EventTypes.MERCHANT_REPORT_GENERATED;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(queue).publish(eventCaptor.capture());
    Event receivedEvent = eventCaptor.getValue();
    merchantReport = receivedEvent.getArgument(0,
            new Report<MerchantView>(new ArrayList<>()) {}.getClass().getGenericSuperclass());

    assertEquals(eventType.getTopic(), receivedEvent.getTopic());
    assertEquals(correlator, receivedEvent.getArgument(1, Correlator.class));
  }
  @And("the payment log is in the merchant report")
  public void thePaymentLogIsInTheMerchantReport() {
    MerchantView merchantView = new MerchantView(paymentRecord.token(),paymentRecord.amount());
    assertTrue(merchantReport.getEntries().contains(merchantView));
  }

}
