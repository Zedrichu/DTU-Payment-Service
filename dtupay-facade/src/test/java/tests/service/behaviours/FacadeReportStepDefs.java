package tests.service.behaviours;

import dtupay.services.facade.domain.ReportService;
import dtupay.services.facade.domain.models.*;
import dtupay.services.facade.domain.models.views.CustomerView;
import dtupay.services.facade.domain.models.views.ManagerView;
import dtupay.services.facade.domain.models.views.MerchantView;
import dtupay.services.facade.utilities.Correlator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadeReportStepDefs {

    private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
    private CompletableFuture<Event> publishedEvent = new CompletableFuture<>();

    private final Function<Event, String> IdKeyExtractor =
            (event) -> event.getArgument(0, String.class);


    private final MessageQueue reportQ = new MockMessageQueue(IdKeyExtractor, publishedEvents);
    private final MessageQueue managerQ = new MessageQueue() {
        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String topic, Consumer<Event> handler) {}
    };

    private ReportService reportService = new ReportService(reportQ);
    private ReportService managerReportService = new ReportService(managerQ);
    private CompletableFuture<Report<CustomerView>> futureCustomerReport = new CompletableFuture<>();
    private CompletableFuture<Report<MerchantView>> futureMerchantReport = new CompletableFuture<>();
    private CompletableFuture<Report<ManagerView>> futureManagerReport = new CompletableFuture<>();

    private Map<String, Correlator> correlators = new ConcurrentHashMap<>();
    private Correlator managerCorrelator;

    private String customerId;
    private String merchantId;
    private String managerId;

    @Given("a customer that has performed no payments")
    public void aCustomerThatHasPerformedNoPayments() {
        customerId = "cId";
        publishedEvents.put(customerId, new CompletableFuture<>());
    }

    @When("a customer is requesting a customer report with a non existing customer id")
    public void aCustomerIsRequestingACustomerReportWithANonExistingCustomerId() {
        new Thread(() -> {
            try {
                var result = reportService.getCustomerReport(customerId);
                futureCustomerReport.complete(result);
            } catch (Exception e) {
                futureCustomerReport.completeExceptionally(e);
            }
        }).start();
    }


    @Then("the {string} event is sent with an non existing customer id")
    public void theEventIsSentWithAnNonExistingCustomerId(String eventType) {
        Event event = publishedEvents.get(customerId).join();
        assertEquals(eventType, event.getTopic());

        var eCustomerId = event.getArgument(0,String.class);
        var correlator = event.getArgument(1,Correlator.class);
        correlators.put(eCustomerId, correlator);
    }

    @Then("the {string} event is received for customer")
    public void aEventIsReceivedWithAnEmptyReport(String eventType) {
        var correlator = correlators.get(customerId);
        Report<CustomerView> customerRep = new Report<>(new ArrayList<CustomerView>());
        assertNotNull(correlator);
        reportService.handleCustomerReportGenerated(new Event(eventType,customerRep,correlator));
    }

    @Then("the retrieved customer report is empty")
    public void theRetrievedCustomerReportIsEmpty() {
        Report<CustomerView> customerRep = futureCustomerReport.join();
        assertEquals(0, customerRep.getEntries().size());
    }

    @Given("a merchant that has performed no payments")
    public void aMerchantThatHasPerformedNoPayments() {
        merchantId = "mId";
        publishedEvents.put(merchantId, new CompletableFuture<>());
    }

    @When("a merchant is requesting a merchant report with a non existing customer id")
    public void aMerchantIsRequestingAMerchantReportWithANonExistingCustomerId() {
        new Thread(() -> {
            try {
                var result = reportService.getMerchantReport(merchantId);
                futureMerchantReport.complete(result);
            } catch (Exception e) {
                futureMerchantReport.completeExceptionally(e);
            }
        }).start();
    }

    @Then("the {string} event is sent with an non existing merchant id")
    public void theEventIsSentWithAnNonExistingMerchantId(String eventType) {
        Event event = publishedEvents.get(merchantId).join();
        assertEquals(eventType, event.getTopic());

        var eMerchantId = event.getArgument(0,String.class);
        var correlator = event.getArgument(1,Correlator.class);
        correlators.put(eMerchantId, correlator);
    }

    @When("the {string} event is received for merchant")
    public void theEventIsReceivedForMerchant(String eventType) {
        var correlator = correlators.get(merchantId);
        Report<MerchantView> merchantRep = new Report<>(new ArrayList<MerchantView>());
        assertNotNull(correlator);
        reportService.handleMerchantReportGenerated(new Event(eventType,merchantRep,correlator));
    }

    @Then("the retrieved merchant report is empty")
    public void theRetrievedMerchantReportIsEmpty() {
        Report<MerchantView> merchantRep = futureMerchantReport.join();
        assertEquals(0, merchantRep.getEntries().size());
    }

    @When("a manager is requesting a manager report")
    public void aManagerIsRequestingAManagerReport() {
        new Thread(() -> {
            try {
                var result = managerReportService.getManagerReport();
                futureManagerReport.complete(result);
            } catch (Exception e) {
                futureManagerReport.completeExceptionally(e);
            }
        }).start();
    }

    @Then("the {string} event is sent")
    public void theEventIsSent(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());

        var correlator = event.getArgument(0,Correlator.class);
        managerCorrelator = correlator;
    }

    @When("the {string} event is received for manager")
    public void theEventIsReceivedForManager(String eventType) {
        var correlator = managerCorrelator;
        Report<ManagerView> managerRep = new Report<>(new ArrayList<ManagerView>());
        assertNotNull(correlator);
        managerReportService.handleManagerReportGenerated(new Event(eventType,managerRep,correlator));
    }

    @Then("the retrieved manager report is empty")
    public void theRetrievedManagerReportIsEmpty() {
        Report<ManagerView> managerRep = futureManagerReport.join();
        assertEquals(0, managerRep.getEntries().size());
    }
}
