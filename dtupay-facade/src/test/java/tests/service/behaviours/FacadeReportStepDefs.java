package tests.service.behaviours;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.ReportService;
import dtupay.services.facade.domain.models.*;
import dtupay.services.facade.domain.models.views.CustomerView;
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
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadeReportStepDefs {

    private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();


    private final Function<Event, String> tokenKeyExtractor =
            (event) -> event.getArgument(0, String.class);


    private final MessageQueue reportCustomerQ = new MockMessageQueue(tokenKeyExtractor, publishedEvents);

    private ReportService reportService = new ReportService(reportCustomerQ);
    private CompletableFuture<Report<CustomerView>> futureCustomerReport = new CompletableFuture<>();

    private Map<String, Correlator> cCorrelators = new ConcurrentHashMap<>();

    private String customerId;
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
        cCorrelators.put(eCustomerId, correlator);
    }

    @Then("the {string} event is received for customer")
    public void aEventIsReceivedWithAnEmptyReport(String eventType) {
        var correlator = cCorrelators.get(customerId);
        Report<CustomerView> customerRep = new Report<>(new ArrayList<CustomerView>());
        assertNotNull(correlator);
        reportService.handleCustomerReportGenerated(new Event(eventType,customerRep,correlator));
    }

    @Then("the retrieve report is empty")
    public void theRetrieveReportIsEmpty() {
        Report<CustomerView> customerRep = futureCustomerReport.join();
        assertEquals(0, customerRep.getEntries().size());
    }
}
