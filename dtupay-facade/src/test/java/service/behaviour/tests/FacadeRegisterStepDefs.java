package service.behaviour.tests;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    private String futureCustomerId2;
    private Event mockEvent;
    private CompletableFuture<Customer> futureCustomer = new CompletableFuture<>();
    private CompletableFuture<Customer> futureCustomer2 = new CompletableFuture<>();
    private EventTypes eventTypeName;
    private Map<Customer, Correlator> cCorrelators = new ConcurrentHashMap<>();
    private Map<Merchant, Correlator> mCorrelators = new ConcurrentHashMap<>();


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

    @When("the second customer is being registered")
    public void theSecondCustomerIsBeingRegistered() {
        new Thread(() -> {
            var result = customerService.register(customer2);
            futureCustomer2.complete(result);
        }).start();
    }

    @Then("the {string} event for the second customer is sent")
    public void theEventForTheSecondCustomerIsSent(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        Event event = publishedEvents.get(id).join();
        assertEquals(eventTypeName.getTopic(), event.getTopic());

        var cust = event.getArgument(0, Customer.class);
        var correlator = event.getArgument(1, Correlator.class);
        cCorrelators.put(cust, correlator);
    }

    @When("the {string} event is received for second customer with non-empty id")
    public void theEventIsReceivedForSecondCustomerWithNonEmptyId(String arg0) {
        eventTypeName = EventTypes.fromTopic(arg0);
        var correlator = cCorrelators.get(customer2);
        assertNotNull(correlator);
        var newCustomer = new Customer(customer2.firstName(), customer2.lastName(), customer2.cpr(), customer2.bankAccountNo(), "5266734512");
        customerService.handleCustomerAccountCreated(new Event(eventTypeName.getTopic(),
              new Object[] {newCustomer, cCorrelators.get(customer2)}));
    }

    @And("the second customer is registered and his id is set")
    public void theSecondCustomerIsRegisteredAndHisIdIsSet() {
        futureCustomerId2 = futureCustomer2.join().payId();
        assertNotNull(futureCustomerId2);
    }

    @And("the customer IDs are different")
    public void theCustomerIDsAreDifferent() {
        assertNotEquals(futureCustomerId, futureCustomerId2);
    }

    @Given("a customer with name {string}, a CPR number {string}, no bank account and empty id")
    public void aCustomerWithNameACPRNumberNoBankAccountAndEmptyId(String name, String cpr) {
        customer = new Customer(name, "", cpr, null, null);

        mockEvent = new Event(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), new Object[]{ customer });
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        publishedEvents.put(id, new CompletableFuture<>());

        assertNull(customer.payId());
    }

    @When("the {string} event is received for the customer")
    public void theEventIsReceivedForTheCustomer(String eventName) {
        eventTypeName = EventTypes.fromTopic(eventName);
        var correlator = cCorrelators.get(customer);
        var errorMessage = "Account creation failed: Provided customer must have a valid bank account number and CPR";
        assertNotNull(correlator);
        customerService.handleCustomerAccountCreationFailed(new Event(eventTypeName.getTopic(),
              new Object[] {errorMessage, cCorrelators.get(customer)}));

    }

    @Then("an exception raises with error message {string}")
    public void anExceptionRaisesWithErrorMessage(String arg0) {
        try {
            futureCustomerId = futureCustomer.join().payId();
        } catch (CompletionException exception) {
            assertNotNull(exception);
            assertTrue(exception.getCause() instanceof AccountCreationException);
            assertEquals(arg0, exception.getCause().getMessage());
        }
    }

    private final BiFunction<Event, String, String> merchantKeyExtractor = (event, flag) ->
          flag + "_|_" + event.getArgument(0, Merchant.class).cpr();

    private final MessageQueue merchantQ = new MockMessageQueue(merchantKeyExtractor, flags);


    private CompletableFuture<Merchant> futureMerchant = new CompletableFuture<>();
    private Merchant merchant;
    private MerchantService merchantService = new MerchantService(merchantQ);


    @Given("a merchant with name {string}, a CPR number {string}, a bank account and empty id")
    public void aMerchantWithNameACPRNumberABankAccountAndEmptyId(String name, String cpr) {
        merchant = new Merchant(name, "", cpr, "123124", null);

        mockEvent = new Event(EventTypes.MERCHANT_REGISTRATION_REQUESTED.getTopic(), new Object[]{ merchant });
        String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        publishedEvents.put(id, new CompletableFuture<>());
        assertNull(merchant.payId());
    }

    @When("the merchant is being registered")
    public void theMerchantIsBeingRegistered() {
        new Thread(() -> {
            try {
                var result = merchantService.register(merchant);
                futureMerchant.complete(result);
            } catch (Exception e) {
                futureMerchant.completeExceptionally(e);
            }
        }).start();
    }

    @Then("the {string} event for the merchant is sent")
    public void theEventForTheMerchantIsSent(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        String id = merchantKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        Event event = publishedEvents.get(id).join();
        assertEquals(eventTypeName.getTopic(), event.getTopic());
        var merch = event.getArgument(0, Merchant.class);
        var correlator = event.getArgument(1, Correlator.class);
        mCorrelators.put(merch, correlator);
    }

    @When("the {string} event is received for merchant with non-empty id")
    public void theEventIsReceivedForMerchantWithNonEmptyId(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        var correlator = mCorrelators.get(merchant);
        assertNotNull(correlator);
        var newMerchant = new Merchant(
              merchant.firstName(),
              merchant.lastName(),
              merchant.cpr(),
              merchant.bankAccountNo(),
              "1");
        merchantService.handleMerchantAccountCreated(new Event(eventTypeName.getTopic(),
              new Object[] { newMerchant, mCorrelators.get(merchant)} ));
    }

    @Then("the merchant is registered and their id is set")
    public void theMerchantIsRegisteredAndTheirIdIsSet() {
        String futureMerchantId = futureMerchant.join().payId();
        assertNotNull(futureMerchantId);
    }

    @Given("a merchant with name {string}, a CPR number {string}, no bank account and empty id")
    public void aMerchantWithNameACPRNumberNoBankAccountAndEmptyId(String firstName, String cpr) {
        merchant = new Merchant(firstName, "FAILING", cpr, "123", null);
        mockEvent = new Event(EventTypes.MERCHANT_REGISTRATION_REQUESTED.getTopic(), new Object[]{ merchant });
        String id = merchantKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

        publishedEvents.put(id, new CompletableFuture<>());
        assertNull(merchant.payId());
    }

    @When("the {string} event is received for the merchant")
    public void theEventIsReceivedForTheMerchant(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        var correlator = mCorrelators.get(merchant);
        var errorMessage = "Account creation failed: Provided merchant must have a valid bank account number and CPR";
        assertNotNull(correlator);
        merchantService.handleMerchantAccountCreationFailed(new Event(eventTypeName.getTopic(),
              new Object[] {errorMessage, mCorrelators.get(merchant)}));
    }

    @Then("an exception raises with the merchant declined error message {string}")
    public void anExceptionRaisesWithTheMerchantDeclinedErrorMessage(String message) {
        try {
            futureMerchant.join();
        } catch (CompletionException exception) {
            assertNotNull(exception);
            assertTrue(exception.getCause() instanceof AccountCreationException);
            assertEquals(message, exception.getCause().getMessage());
        }
    }

}
