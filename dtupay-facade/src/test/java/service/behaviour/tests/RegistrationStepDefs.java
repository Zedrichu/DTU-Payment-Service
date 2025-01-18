package service.behaviour.tests;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.utilities.Correlator;
import io.cucumber.java.en.And;
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

import static org.junit.Assert.*;

public class RegistrationStepDefs {

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private Map<Customer, Correlator> cCorrelators = new ConcurrentHashMap<>();
	private Map<Merchant, Correlator> mCorrelators = new ConcurrentHashMap<>();

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
	private MerchantService merchantService = new MerchantService(q);

	private Customer customer;
	private Customer customer2;
	private CompletableFuture<Customer> futureCustomer = new CompletableFuture<>();
	private CompletableFuture<Customer> futureCustomer2 = new CompletableFuture<>();

	@Given("a customer with name {string}, a CPR number {string}, a bank account and empty id")
	public void aCustomerWithNameACPRNumberABankAccountAndEmptyId(String firstName, String cpr) {
		customer = new Customer(firstName, "", cpr, "123", null);
		publishedEvents.put(customer.firstName(), new CompletableFuture<>());
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
		Event event = publishedEvents.get(customer.firstName()).join();
		assertEquals(eventType, event.getTopic());

		var cust = event.getArgument(0, Customer.class);
		var correlator = event.getArgument(1, Correlator.class);
		cCorrelators.put(cust, correlator);
	}

	@When("the {string} event is received for customer with non-empty id")
	public void theEventIsReceivedWithNonEmptyId(String arg0) {
		//simulation of event
		var correlator = cCorrelators.get(customer);
		assertNotNull(correlator);
		var newCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), "1234512");
		customerService.handleCustomerAccountCreated(new Event(arg0,
					new Object[] {newCustomer, cCorrelators.get(customer)}));
	}

	private String futureCustomerId;
	private Exception exception;

	@Then("the customer is registered and his id is set")
	public void theCustomerIsRegisteredAndHisIdIsSet() {
		futureCustomerId = futureCustomer.join().payId();
		assertNotNull(futureCustomerId);
	}


	@Given("a second customer with name {string}, a CPR number {string}, a bank account and empty id")
	public void aSecondCustomerWithNameACPRNumberABankAccountAndEmptyId(String name, String cpr) {
		customer2 = new Customer(name, "", cpr, "104", null);
		publishedEvents.put(customer2.firstName(), new CompletableFuture<>());
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
		Event event = publishedEvents.get(customer2.firstName()).join();
		assertEquals(eventType, event.getTopic());

		var cust = event.getArgument(0, Customer.class);
		var correlator = event.getArgument(1, Correlator.class);
		cCorrelators.put(cust, correlator);
	}

	@When("the {string} event is received for second customer with non-empty id")
	public void theEventIsReceivedForSecondCustomerWithNonEmptyId(String arg0) {
		var correlator = cCorrelators.get(customer2);
		assertNotNull(correlator);
		var newCustomer = new Customer(customer2.firstName(), customer2.lastName(), customer2.cpr(), customer2.bankAccountNo(), "5266734512");
		customerService.handleCustomerAccountCreated(new Event(arg0,
					new Object[] {newCustomer, cCorrelators.get(customer2)}));
	}

	private String futureCustomerId2;

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
		publishedEvents.put(customer.firstName(), new CompletableFuture<>());
		assertNull(customer.payId());
	}

	@When("the {string} event is received for the customer")
	public void theEventIsReceivedForTheCustomer(String eventName) {
		var correlator = cCorrelators.get(customer);
		var errorMessage = "Account creation failed: Provided customer must have a valid bank account number and CPR";
		assertNotNull(correlator);
		customerService.handleCustomerAccountCreationFailed(new Event(eventName,
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

	private Merchant merchant;
	private CompletableFuture<Merchant> futureMerchant = new CompletableFuture<>();

	@Given("a merchant with name {string}, a CPR number {string}, a bank account and empty id")
	public void aMerchantWithNameACPRNumberABankAccountAndEmptyId(String name, String cpr) {
		merchant = new Merchant(name, "", cpr, "123124", null);
		publishedEvents.put(merchant.firstName(), new CompletableFuture<>());
		assertNull(merchant.payId());
	}

	@When("the merchant is being registered")
	public void theMerchantIsBeingRegistered() {
		new Thread(() -> {
			var result = merchantService.register(merchant);
			futureMerchant.complete(result);
		}).start();
	}

	@Then("the {string} event for the merchant is sent")
	public void theEventForTheMerchantIsSent(String eventType) {
		Event event = publishedEvents.get(merchant.firstName()).join();
		assertEquals(eventType, event.getTopic());
		var merch = event.getArgument(0, Merchant.class);
		var correlator = event.getArgument(1, Correlator.class);
		mCorrelators.put(merch, correlator);
	}

	@When("the {string} event is received for merchant with non-empty id")
	public void theEventIsReceivedForMerchantWithNonEmptyId(String eventType) {
		var correlator = mCorrelators.get(merchant);
		assertNotNull(correlator);
		var newMerchant = new Merchant(
					merchant.firstName(),
					merchant.lastName(),
					merchant.cpr(),
					merchant.bankAccountNo(),
					"1");
		merchantService.handleMerchantAccountCreated(new Event(eventType,
					new Object[] { newMerchant, mCorrelators.get(merchant)} ));
	}

	private String futureMerchantId;
	@Then("the merchant is registered and their id is set")
	public void theMerchantIsRegisteredAndTheirIdIsSet() {
		futureMerchantId = futureMerchant.join().payId();
		assertNotNull(futureMerchantId);
	}
}
