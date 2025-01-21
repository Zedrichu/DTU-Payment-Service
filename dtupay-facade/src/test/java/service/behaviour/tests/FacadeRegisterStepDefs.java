package service.behaviour.tests;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class FacadeRegisterStepDefs {

    private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();

    private final Map<String, String> flags = new HashMap<>() {{
        put(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), "CRR");
        put(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), "CDR");
        put(EventTypes.PAYMENT_INITIATED.getTopic(), "PIN");
        put(EventTypes.TOKENS_REQUESTED.getTopic(), "TKR");
        put(EventTypes.MERCHANT_REGISTRATION_REQUESTED.getTopic(), "MRR");
        put(EventTypes.MERCHANT_ACCOUNT_CREATION_FAILED.getTopic(), "MAF");
    }};

    public class MockMessageQueue implements MessageQueue {

        private final BiFunction<Event, String, String> idExtractor;

        public MockMessageQueue(BiFunction<Event, String, String> mapping, Map<String, String> flags) {
            this.idExtractor = mapping;
        }

        public String getFlag(String key) {
            return flags.get(key);
        }

        @Override
        public void publish(Event event) {
            String id = idExtractor.apply(event, getFlag(event.getTopic()));
            if (publishedEvents.containsKey(id)) {
                publishedEvents.get(id).complete(event);
            }
        }

        @Override
        public void addHandler(String topic, Consumer<Event> handler) {}
    }

    private final BiFunction<Event, String, String> customerKeyExtractor = (event, flag) ->
            flag + "_|_" + event.getArgument(0, Customer.class).cpr();

    private final MessageQueue customerQ = new MockMessageQueue(customerKeyExtractor, flags);

    private CustomerService customerService = new CustomerService(customerQ);
    private Customer customer;
    private Customer customer2;
    private String futureCustomerId;
    private Event mockEvent;
    private CompletableFuture<Customer> futureCustomer = new CompletableFuture<>();
    private EventTypes eventTypeName;
    private Map<Customer, Correlator> cCorrelators = new ConcurrentHashMap<>();


    @Given("a customer with name {string}, a CPR number {string}, a bank account and empty id")
    public void aCustomerWithNameACPRNumberABankAccountAndEmptyId(String firstName, String cpr) {
        customer = new Customer(firstName, "", cpr, "123", null);

        mockEvent = new Event(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), new Object[]{ customer });
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        publishedEvents.put(id, new CompletableFuture<>());
        assertNull(customer.payId());
    }

    @When("the customer is being registered")
    public void theCustomerIsBeingRegistered() {
        new Thread(() -> {
            try {
                var result = customerService.register(customer);
                futureCustomer.complete(result);
            } catch (Exception e) {
                futureCustomer.completeExceptionally(e);
            }
        }).start();
    }

    @Then("the {string} event for the customer is sent")
    public void theEventIsSent(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        Event event = publishedEvents.get(id).join();
        assertEquals(eventTypeName.getTopic(), event.getTopic());
        var cust = event.getArgument(0, Customer.class);
        var correlator = event.getArgument(1, Correlator.class);
        cCorrelators.put(cust, correlator);
    }

    @When("the {string} event is received for customer with non-empty id")
    public void theEventIsReceivedWithNonEmptyId(String arg0) {
        eventTypeName = EventTypes.fromTopic(arg0);
        //simulation of event
        var correlator = cCorrelators.get(customer);
        assertNotNull(correlator);
        var newCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), "1234512");
        customerService.handleCustomerAccountCreated(new Event(eventTypeName.getTopic(),
                new Object[] {newCustomer, cCorrelators.get(customer)}));
    }

    @Then("the customer is registered and his id is set")
    public void theCustomerIsRegisteredAndHisIdIsSet() {
        futureCustomerId = futureCustomer.join().payId();
        assertNotNull(futureCustomerId);
    }

    @Given("a second customer with name {string}, a CPR number {string}, a bank account and empty id")
    public void aSecondCustomerWithNameACPRNumberABankAccountAndEmptyId(String name, String cpr) {
        customer2 = new Customer(name, "", cpr, "104", null);

        mockEvent = new Event(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), new Object[]{ customer2 });
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        publishedEvents.put(id, new CompletableFuture<>());
        assertNull(customer2.payId());
    }
}
