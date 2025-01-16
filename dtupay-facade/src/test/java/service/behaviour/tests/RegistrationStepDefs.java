package service.behaviour.tests;

import dtupay.facade.domain.CustomerService;
import dtupay.facade.domain.models.Customer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class RegistrationStepDefs {

	private CompletableFuture<Event> publishedEvent = new CompletableFuture<>();
	private MessageQueue q = new MessageQueue() {
		@Override
		public void publish(Event event) {
			publishedEvent.complete(event);
		}

		@Override
		public void addHandler(String topic, Consumer<Event> handler) {}
	};

	private CustomerService customerService = new CustomerService(q);
	private Customer customer;
	private CompletableFuture<String> futureCustomerId = new CompletableFuture<>();

	@Given("a customer with name {string}, a CPR number {string}, a bank account and empty id")
	public void aCustomerWithNameACPRNumberABankAccountAndEmptyId(String firstName, String cpr) {
		customer = new Customer(firstName, "", cpr, "123", null);
//		customer.setFirstName(firstName);
//		customer.setCpr(cpr);
		assertNull(customer.id());
	}

	@When("the customer is being registered")
	public void theCustomerIsBeingRegistered() {
		new Thread(() -> {
			var result = customerService.register(customer);
			futureCustomerId.complete(result);
		}).start();
	}

	@Then("the {string} event is sent")
	public void theEventIsSent(String eventType) {
		Event event = new Event(eventType, new Object[]{ customer });
		assertEquals(event, publishedEvent.join());
	}

	@When("the {string} event is received with non-empty id")
	public void theEventIsReceivedWithNonEmptyId(String arg0) {
		//simulation of event
		var c = new Customer(customer.firstName(),
												 customer.lastName(),
													customer.cpr(),
													customer.bankAccountNo(),
													"1234512");
		customerService.handleCustomerRegistered(new Event("..", new Object[] {c}));
	}

	@Then("the customer is registered and his id is set")
	public void theCustomerIsRegisteredAndHisIdIsSet() {
		assertNotNull(futureCustomerId.join());
	}
}
