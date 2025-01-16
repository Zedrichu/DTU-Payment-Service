package service.behaviour.tests;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.utilities.Correlator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class RegistrationStepDefs {

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private Map<Customer, Correlator> correlators = new ConcurrentHashMap<>();
	private MessageQueue q = new MessageQueue() {
		@Override
		public void publish(Event event) {
			var c = event.getArgument(0, Customer.class);
			publishedEvents.get(c.firstName()).complete(event);
		}

		@Override
		public void addHandler(String topic, Consumer<Event> handler) {}
	};

	private CustomerService customerService = new CustomerService(q);

	private Customer customer;
	private Customer customer2;
	private CompletableFuture<Customer> futureCustomer = new CompletableFuture<>();
	private CompletableFuture<Customer> futureCustomer2 = new CompletableFuture<>();

	@Given("a customer with name {string}, a CPR number {string}, a bank account and empty id")
	public void aCustomerWithNameACPRNumberABankAccountAndEmptyId(String firstName, String cpr) {
		customer = new Customer(firstName, "", cpr, "123", null);
		publishedEvents.put(customer.firstName(), new CompletableFuture<>());
		assertNull(customer.id());
	}

	@When("the customer is being registered")
	public void theCustomerIsBeingRegistered() {
		new Thread(() -> {
			var result = customerService.register(customer);
			futureCustomer.complete(result);
		}).start();
	}

	@Then("the {string} event for the customer is sent")
	public void theEventIsSent(String eventType) {
		Event event = publishedEvents.get(customer.firstName()).join();
		assertEquals(eventType, event.getType());

		var cust = event.getArgument(0, Customer.class);
		var correlator = event.getArgument(1, Correlator.class);
		correlators.put(cust, correlator);
	}

	@When("the {string} event is received for customer with non-empty id")
	public void theEventIsReceivedWithNonEmptyId(String arg0) {
		//simulation of event
		var correlator = correlators.get(customer);
		assertNotNull(correlator);
		var newCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), "1234512");
		customerService.handleCustomerAccountCreated(new Event(arg0,
					new Object[] {newCustomer, correlators.get(customer)}));
	}

	private String futureCustomerId;

	@Then("the customer is registered and his id is set")
	public void theCustomerIsRegisteredAndHisIdIsSet() {
		futureCustomerId = futureCustomer.join().id();
		assertNotNull(futureCustomerId);
	}


	@Given("a second customer with name {string}, a CPR number {string}, a bank account and empty id")
	public void aSecondCustomerWithNameACPRNumberABankAccountAndEmptyId(String name, String cpr) {
		customer2 = new Customer(name, "", cpr, "104", null);
		publishedEvents.put(customer2.firstName(), new CompletableFuture<>());
		assertNull(customer2.id());
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
		Event event = publishedEvents.get(customer2.firstName()).join();
		assertEquals(eventType, event.getType());

		var cust = event.getArgument(0, Customer.class);
		var correlator = event.getArgument(1, Correlator.class);
		correlators.put(cust, correlator);
	}

	@When("the {string} event is received for second customer with non-empty id")
	public void theEventIsReceivedForSecondCustomerWithNonEmptyId(String arg0) {
		var correlator = correlators.get(customer2);
		assertNotNull(correlator);
		var newCustomer = new Customer(customer2.firstName(), customer2.lastName(), customer2.cpr(), customer2.bankAccountNo(), "5266734512");
		customerService.handleCustomerAccountCreated(new Event(arg0,
					new Object[] {newCustomer, correlators.get(customer2)}));
	}

	private String futureCustomerId2;

	@And("the second customer is registered and his id is set")
	public void theSecondCustomerIsRegisteredAndHisIdIsSet() {
		futureCustomerId2 = futureCustomer2.join().id();
		assertNotNull(futureCustomerId2);
	}

	@And("the customer IDs are different")
	public void theCustomerIDsAreDifferent() {
		assertNotEquals(futureCustomerId, futureCustomerId2);
	}

}
