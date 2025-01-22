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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

	private final MessageQueue customerIdQ = new MockMessageQueue(customerIdKeyExtractor);
	private CustomerService customerIdService = new CustomerService(customerIdQ);

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private CompletableFuture<String> futureCustomerDeregister = new CompletableFuture<>();
	private Map<String, Correlator> cStringCorrelators = new ConcurrentHashMap<>();
	private Event mockEvent;
	private Customer customer;
	private EventTypes eventTypeName;
	private EventTypes eventTypeName1;

	@Given("a registered customer with id opting to deregister")
	public void aRegisteredCustomerWithTokensOptingToDeregister() {
		customer = new Customer("Lars", "", "011298-1136", "123", "reqid");

		mockEvent = new Event(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ customer.payId() });
		String id = customerIdKeyExtractor.apply(mockEvent);

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(customer.payId());
	}

	@When("the customer is being deregistered")
	public void theCustomerIsBeingDeregistered() {
		new Thread(() -> {
			var result = customerIdService.deregister(customer.payId());
			futureCustomerDeregister.complete(result);
		}).start();
	}

	@Then("the {string} event for the customer is sent with their id")
	public void theEventForTheCustomerIsSentWithTheirId(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
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
		customerIdService.handleCustomerDeregistered(new Event(eventTypeName.getTopic(), new Object[]{ customer.payId(), correlator }));
	}

	@Then("the customer is deregistered")
	public void theCustomerIsDeregisteredAndTheirTokensAreRemoved() {
		var result = futureCustomerDeregister.join();
		assertEquals("Customer Successful Deregistration", result);
	}
}
