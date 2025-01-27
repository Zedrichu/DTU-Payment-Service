package tests.service.behaviours;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadeDeregisterStepDefs {

	public class MockMessageQueue implements MessageQueue {

		private final Function<Event, String> idExtractor;

		public MockMessageQueue(Function<Event, String> mapping) {
			this.idExtractor = mapping;
		}

		@Override
		public void publish(Event event) {
			String id = idExtractor.apply(event);
			if (publishedEvents.containsKey(id)) {
				publishedEvents.get(id).complete(event);
			}
		}

		@Override
		public void addHandler(String topic, Consumer<Event> handler) {}
	}

	private final Function<Event, String> customerIdKeyExtractor =
				(event) -> event.getArgument(0, String.class);
	private final Function<Event, String> merchantIdKeyExtractor =
				(Event event) -> event.getArgument(0, String.class);

	private final MessageQueue customerIdQ = new MockMessageQueue(customerIdKeyExtractor);
	private final MessageQueue merchantIdQ = new MockMessageQueue(merchantIdKeyExtractor);
	private CustomerService customerIdService = new CustomerService(customerIdQ);
	private MerchantService merchantIdService = new MerchantService(merchantIdQ);

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private CompletableFuture<Boolean> futureCustomerDeregister = new CompletableFuture<>();
	private CompletableFuture<Boolean> futureMerchantDeregister = new CompletableFuture<>();
	private Map<String, Correlator> cStringCorrelators = new ConcurrentHashMap<>();
	private Map<String, Correlator> mStringCorrelators = new ConcurrentHashMap<>();
	private Event mockEvent;
	private Customer customer;
	private Merchant merchant;
	private EventTypes eventTypeName;

	@Given("a registered customer with id opting to deregister")
	public void aRegisteredCustomerWithTokensOptingToDeregister() {
		customer = new Customer("Lars", "", "011298-1136", "123", "reqid");

		mockEvent = new Event(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ customer.payId() });
		String id = customerIdKeyExtractor.apply(mockEvent);

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(customer.payId());
	}

	Throwable exception;

	@When("the customer is being deregistered")
	public void theCustomerIsBeingDeregistered() {
		new Thread(() -> {
			try{
				var result = customerIdService.deregister(customer.payId());
				futureCustomerDeregister.complete(result);
			}catch (Exception e){
				futureCustomerDeregister.completeExceptionally(e.getCause());
			}

		}).start();
	}

	@Then("the {string} event for the customer is sent with their id")
	public void theEventForTheCustomerIsSentWithTheirId(String eventType) {
		eventTypeName = EventTypes.valueOf(eventType);
		String id = customerIdKeyExtractor.apply(mockEvent);

		Event event = publishedEvents.get(id).join();
		assertEquals(eventTypeName.getTopic(), event.getTopic());
		var cust = event.getArgument(0, String.class);
		var correlator = event.getArgument(1, Correlator.class);
		cStringCorrelators.put(cust, correlator);
	}

	@When("the {string} event is received for the customer id")
	public void theEventIsReceivedForTheCustomerId(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		var correlator = cStringCorrelators.get(customer.payId());
		assertNotNull(correlator);
		customerIdService.handleCustomerDeregistered(new Event(eventTypeName.getTopic(), new Object[]{  correlator }));
	}

	@Then("the customer is deregistered")
	public void theCustomerIsDeregistered() {
		var result = futureCustomerDeregister.join();
		assertTrue(result);
	}

	@Then("the customer deregistration failed")
	public void theCustomerDeregistrationFailed() {
		try {
			futureCustomerDeregister.join();
		} catch (CompletionException e) {
			exception = e.getCause();
		}
		assertNotNull(exception);
		assertEquals("Customer Deregistration Failed", exception.getMessage());
	}

	@Given("a registered merchant with id opting to deregister")
	public void aRegisteredMerchantWithIdOptingToDeregister() {
		merchant = new Merchant("Elong", "Ma", "011298-1135", "13", "reqid");

		mockEvent = new Event(EventTypes.MERCHANT_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ merchant.payId() });
		String id = merchantIdKeyExtractor.apply(mockEvent);

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(merchant.payId());
	}

	@When("the merchant is being deregistered")
	public void theMerchantIsBeingDeregistered() {
		new Thread(() -> {
			try{
				var result = merchantIdService.deregister(merchant.payId());
				futureMerchantDeregister.complete(result);
			} catch (Exception e) {
				futureMerchantDeregister.completeExceptionally(e.getCause());
			}
		}).start();
	}

	@Then("the {string} event for the merchant is sent with their id")
	public void theEventForTheMerchantIsSentWithTheirId(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		String id = merchantIdKeyExtractor.apply(mockEvent);
		Event event = publishedEvents.get(id).join();
		assertEquals(eventTypeName.getTopic(), event.getTopic());
		var merc = event.getArgument(0, String.class);
		var correlator = event.getArgument(1, Correlator.class);
		mStringCorrelators.put(merc, correlator);
	}

	@When("the {string} event is received for the merchant id")
	public void theEventIsReceivedForTheMerchantId(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		var correlator = mStringCorrelators.get(merchant.payId());
		assertNotNull(correlator);
		merchantIdService.handleMerchantDeregistered(new Event(eventTypeName.getTopic(), new Object[]{  correlator }));
	}

	@Then("the merchant is deregistered")
	public void theMerchantIsDeregistered() {
		var result = futureMerchantDeregister.join();
		assertTrue(result);
	}

	@Then("the merchant deregistration failed")
	public void theMerchantDeregistrationFailed() {
		try {
			futureMerchantDeregister.join();
		} catch (CompletionException e) {
			exception = e.getCause();
		}
		assertNotNull(exception);
		assertEquals("Merchant Deregistration Failed", exception.getMessage());
	}
}
