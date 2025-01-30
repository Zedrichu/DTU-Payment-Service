package service.behaviours.tests;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.Token;
import dtupay.services.reporting.domain.repositories.LedgerWriteRepository;
import dtupay.services.reporting.domain.repositories.LedgerReadRepository;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import messaging.Event;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import static org.mockito.Mockito.mock;

public class ReportingStepDefs {
    private ReportingManager reportingManager;
    private LedgerReadRepository ledgerReadRepository;
    private LedgerWriteRepository reportRepository;
    private PaymentRecord paymentRecord;
    messaging.MessageQueue messageQueue = mock(messaging.MessageQueue.class);
    MessageQueue internalMQ = mock(MessageQueue.class);


  @Given("a reporting service")
  public void aReportingService() {
    ledgerReadRepository = new LedgerReadRepository(internalMQ);
    reportRepository = new LedgerWriteRepository(internalMQ);
    reportingManager = new ReportingManager(messageQueue, ledgerReadRepository, reportRepository);
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
            Token.random(), // TODO: replace with updated token
            "cId",
            "mId"
            );


    reportingManager.handleBankTransferConfirmed(new Event(eventType.getTopic(),new Object[] {paymentRecord, correlationId}));
  }

//  @Then("the payment history is updated")
//  public void thePaymentHistoryIsUpdated() {
//    reportingManager.
//  }


}
