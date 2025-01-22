package service.behaviours.tests;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.domain.repositories.PaymentLogRepository;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import static org.mockito.Mockito.mock;

public class ReportingStepDefs {
    private ReportingManager reportingManager;
    private ReadModelRepository readRepository;
    private PaymentLogRepository writeRepository;
    private PaymentRecord paymentRecord;
    MessageQueue messageQueue = mock(MessageQueue.class);


  @Given("a reporting service")
  public void aReportingService() {
    readRepository = new ReadModelRepository();
    writeRepository = new PaymentLogRepository(messageQueue);
    reportingManager = new ReportingManager(messageQueue, readRepository, writeRepository);
  }

  @When("a BankTransferConfirmed event is received")
  public void aBankTransferConfirmedEventIsReceived() {
    EventTypes eventType = EventTypes.BANK_TRANSFER_CONFIRMED;
    var correlationId = Correlator.random();
    paymentRecord = new PaymentRecord(
            "cBankAccount",
            "mBankAccount",
            1000,
            "Payment happened",
            "Token", // TODO: replace with updated token
            "cId",
            "mId"
            );
    Event event = new Event(eventType.getTopic(),
            new Object[]{paymentRecord, correlationId});

     reportingManager.handleBankTransferConfirmed(event);
  }

  @Then("the payment history is updated")
  public void thePaymentHistoryIsUpdated() {
        //reportingManager.paymentByCustomerId("cId");
  }


}
