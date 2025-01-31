package service.behaviours.tests;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.reporting.application.services.ReportingManager;
import dtupay.services.reporting.domain.entities.LedgerAggregate;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.models.Token;
import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.query.projection.LedgerViewProjector;
import dtupay.services.reporting.query.projection.ReportProjection;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import messaging.Event;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ReportingStepDefs {
    private ReportingManager reportingManager;
    private PaymentRecord paymentRecord;
    messaging.MessageQueue dtupayMQ = mock(messaging.MessageQueue.class);
    MessageQueue internalMQ = new MessageQueueAsync();
    private Correlator correlationId;
    private ArgumentCaptor<Event> eventCaptor;


  @Before
  public void aReportingService() {
    LedgerWriteRepository writeRepository = new LedgerWriteRepository(internalMQ);
    LedgerReadRepository readRepository = new LedgerReadRepository();

    new LedgerViewProjector(writeRepository, readRepository, internalMQ);

    reportingManager = new ReportingManager(dtupayMQ, readRepository, writeRepository);
  }

  private String customerId;
  private String merchantId;
  private ArrayList<CustomerView> receivedCViews;
  private ArrayList<MerchantView> receivedMViews;
  private ArrayList<ManagerView> receivedManViews;
  private CustomerView cView;
  private MerchantView mView;
  private ManagerView managerView;

  @When("a BankTransferConfirmed event is received")
  public void aBankTransferConfirmedEventIsReceived() throws InterruptedException {
    EventTypes eventType = EventTypes.BANK_TRANSFER_CONFIRMED;
    correlationId = Correlator.random();

    customerId = "cId";
    merchantId = "mId";

    paymentRecord = new PaymentRecord(
            "cBankAccount",
            "mBankAccount",
            1000,
            "Payment happened",
            Token.random(),
            customerId,
            merchantId
            );


    reportingManager.handleBankTransferConfirmed(new Event(eventType.getTopic(), paymentRecord, correlationId));
    Thread.sleep(1000);
  }

  @Then("the report projection is updated with a view for each role")
  public void theReportProjectionIsUpdatedWithAViewForEachRole() throws InterruptedException {
    var projection = reportingManager.getProjection();
    cView = new CustomerView(paymentRecord.amount(), paymentRecord.merchantId(), paymentRecord.token());
    mView = new MerchantView(paymentRecord.amount(), paymentRecord.token());
    managerView = new ManagerView(paymentRecord.customerId(), paymentRecord.merchantId(), paymentRecord.token(), paymentRecord.amount());

    var projCViews = projection.getCustomerViews(customerId);
    var projMViews = projection.getMerchantViews(merchantId);
    var projManagers = projection.getManagerViews();

    assertEquals(1, projCViews.size());
    assertEquals(1, projMViews.size());
    assertEquals(1, projManagers.size());

    assertTrue(projection.getCustomerViews(customerId).contains(cView));
    assertTrue(projection.getMerchantViews(merchantId).contains(mView));
    assertTrue(projection.getManagerViews().contains(managerView));
  }

  @When("the CustomerReportRequested event is received for an id")
  public void theCustomerReportRequestedEventIsReceivedForAnId() {
    var eventType = EventTypes.CUSTOMER_REPORT_REQUESTED;
    Event event = new Event(eventType.getTopic(), customerId, correlationId);
    reportingManager.handleCustomerReportRequested(event);
  }


  @Then("the CustomerReportGenerated event is sent with same correlation id and views")
  public void theCustomerReportGeneratedEventIsSentWithSameCorrelationIdAndViews() {
    EventTypes type = EventTypes.CUSTOMER_REPORT_GENERATED;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(dtupayMQ).publish(eventCaptor.capture());

    Event receivedEvent = eventCaptor.getAllValues().stream().filter(item -> item.getTopic().equals(type.getTopic())).findFirst().get();
    ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
    Gson gson = new Gson();
    List<CustomerView> tokenList = list.stream()
          .map(token -> gson.fromJson(gson.toJson(token), CustomerView.class))
          .toList();
    receivedCViews = new ArrayList<>(tokenList);

    assertEquals(type.getTopic(), receivedEvent.getTopic());
    assertEquals(correlationId, receivedEvent.getArgument(1, Correlator.class));
  }

  @And("the customer views contain the transaction received")
  public void theViewsContainsTheTransactionReceived() {
    assertEquals(1, receivedCViews.size());
    assertTrue(receivedCViews.contains(cView));
  }

  @When("the MerchantReportRequested event is received for an id")
  public void theMerchantReportRequestedEventIsReceivedForAnId() {
    var eventType = EventTypes.MERCHANT_REPORT_REQUESTED;
    Event event = new Event(eventType.getTopic(), merchantId, correlationId);
    reportingManager.handleMerchantReportRequested(event);
  }

  @Then("the MerchantReportGenerated event is sent with same correlation id and views")
  public void theMerchantReportGeneratedEventIsSentWithSameCorrelationIdAndViews() {
    EventTypes type = EventTypes.MERCHANT_REPORT_GENERATED;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(dtupayMQ, times(2)).publish(eventCaptor.capture());

    Event receivedEvent = eventCaptor.getAllValues().stream().filter(item -> item.getTopic().equals(type.getTopic())).findFirst().get();
    ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
    Gson gson = new Gson();
    List<MerchantView> tokenList = list.stream()
          .map(token -> gson.fromJson(gson.toJson(token), MerchantView.class))
          .toList();
    receivedMViews = new ArrayList<>(tokenList);

    assertEquals(type.getTopic(), receivedEvent.getTopic());
    assertEquals(correlationId, receivedEvent.getArgument(1, Correlator.class));
  }

  @And("the merchant views contain the transaction received")
  public void theMerchantViewsContainTheTransactionReceived() {
    assertEquals(1, receivedMViews.size());
    assertTrue(receivedMViews.contains(mView));
  }

  @When("the ManagerReportRequested event is received for an id")
  public void theManagerReportRequestedEventIsReceivedForAnId() {
    var eventType = EventTypes.MANAGER_REPORT_REQUESTED;
    Event event = new Event(eventType.getTopic(), correlationId);
    reportingManager.handleManagerReportRequested(event);
  }

  @Then("the ManagerReportGenerated event is sent with same correlation id and views")
  public void theManagerReportGeneratedEventIsSentWithSameCorrelationIdAndViews() {
    EventTypes type = EventTypes.MANAGER_REPORT_GENERATED;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    verify(dtupayMQ, times(3)).publish(eventCaptor.capture());

    Event receivedEvent = eventCaptor.getAllValues().stream().filter(item -> item.getTopic().equals(type.getTopic())).findFirst().get();
    ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
    Gson gson = new Gson();
    List<ManagerView> tokenList = list.stream()
          .map(token -> gson.fromJson(gson.toJson(token), ManagerView.class))
          .toList();
    receivedManViews = new ArrayList<>(tokenList);

    assertEquals(type.getTopic(), receivedEvent.getTopic());
    assertEquals(correlationId, receivedEvent.getArgument(1, Correlator.class));
  }

  @And("the manager views contain the transaction received")
  public void theManagerViewsContainTheTransactionReceived() {
    assertEquals(1, receivedManViews.size());
    assertTrue(receivedManViews.contains(managerView));
  }
}
