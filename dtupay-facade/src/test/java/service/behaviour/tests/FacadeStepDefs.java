package service.behaviour.tests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class FacadeStepDefs {

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

	private BiFunction<Event, String, String> customerKeyExtractor = (event, flag) ->
			flag + "_|_" + event.getArgument(0, Customer.class).cpr();

	private MessageQueue customerQ = new MockMessageQueue(customerKeyExtractor, flags);

	private BiFunction<Event, String, String> customerIdKeyExtractor = (event, flag) ->
			flag + "_|_" + event.getArgument(0, String.class);

	private MessageQueue customerIdQ = new MockMessageQueue(customerIdKeyExtractor, flags);


	private BiFunction<Event, String, String> merchantKeyExtractor = (event, flag) ->
			flag + "_|_" + event.getArgument(0, Merchant.class).cpr();

	private MessageQueue merchantQ = new MockMessageQueue(merchantKeyExtractor, flags);


	private BiFunction<Event, String, String> tokenKeyExtractor = (event, flag) ->
			flag + "_|_" + event.getArgument(0, String.class);

	private MessageQueue tokensQ = new MockMessageQueue(tokenKeyExtractor, flags);

	private BiFunction<Event, String, String> paymentKeyExtractor = (event, flag) ->
			flag + "_|_" + event.getArgument(0, PaymentRequest.class).token().toString();

	private MessageQueue paymentQ = new MockMessageQueue(paymentKeyExtractor, flags);


	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private Map<Customer, Correlator> cCorrelators = new ConcurrentHashMap<>();
	private Map<Merchant, Correlator> mCorrelators = new ConcurrentHashMap<>();
	private Map<PaymentRequest, Correlator> payCorrelators = new ConcurrentHashMap<>();

	private CustomerService tokenService = new CustomerService(tokensQ);
	private CustomerService customerService = new CustomerService(customerQ);
	private MerchantService merchantService = new MerchantService(merchantQ);
	private MerchantService payService = new MerchantService(paymentQ);
	private CustomerService customerIdService = new CustomerService(customerIdQ);

	private Customer customer;
	private Customer customer2;
	private CompletableFuture<Customer> futureCustomer = new CompletableFuture<>();
	private CompletableFuture<Customer> futureCustomer2 = new CompletableFuture<>();
	private CompletableFuture<Boolean> futurePaymentSuccess = new CompletableFuture<>();
	private Event mockEvent;
	private EventTypes eventTypeName;

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

	private Merchant merchant;
	private CompletableFuture<Merchant> futureMerchant = new CompletableFuture<>();
	private String futureMerchantId;

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

	private PaymentRequest paymentRequest;

	@Given("a valid payment request")
	public void aValidPaymentRequest() {
		String merchantId = "123182752138-mid";
		UUID token = UUID.randomUUID();
		int amount = 50;
		paymentRequest = new PaymentRequest(merchantId, new Token(token), amount);

		mockEvent = new Event(EventTypes.PAYMENT_INITIATED.getTopic(), new Object[]{ paymentRequest });
		String id = paymentKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(paymentRequest.merchantId());
		assertTrue(paymentRequest.amount() > 0);
	}

	@When("the payment request is initiated")
	public void aValidPaymentRequestIsInitiated() {
		new Thread(() -> {
			var success = payService.pay(paymentRequest);
			futurePaymentSuccess.complete(success);
		}).start();
	}

	@Then("the {string} event for the payment request is sent")
	public void theEventForThePaymentRequestIsSent(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		String id = paymentKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		Event event = publishedEvents.get(id).join();
		assertEquals(eventTypeName.getTopic(), event.getTopic());
		var payment = event.getArgument(0, PaymentRequest.class);
		var correlator = event.getArgument(1, Correlator.class);
		payCorrelators.put(payment, correlator);
	}

	@When("the {string} event is received")
	public void theEventIsReceived(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		var correlator = payCorrelators.get(paymentRequest);
		assertNotNull(correlator);
		String placeholder = "placeholder";
		payService.handleBankTransferConfirmed(new Event(eventTypeName.getTopic(), new Object[] {
				placeholder, payCorrelators.get(paymentRequest)
		}));
	}

	@Then("the payment was successful")
	public void thePaymentWasSuccessful() {
		boolean success = futurePaymentSuccess.join();
		assertTrue(success);
	}

	private CompletableFuture<Customer> futureRegisteredCustomer = new CompletableFuture<>();
	private ArrayList<Token> tokens = new ArrayList<>();
	private Customer registeredCustomer;
	private CompletableFuture<ArrayList<Token>> futureTokenRequest = new CompletableFuture<>();
	private Map<String, Correlator> tCorrelators = new ConcurrentHashMap<>();

	@Given("a registered customer with {int} tokens")
	public void aRegisteredCustomerWithTokens(int initialTokens) {
		customer = new Customer("Lars", "", "011298-1136", "123", null);

		mockEvent = new Event(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), new Object[]{ customer });
		String id = customerKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		publishedEvents.put(id, new CompletableFuture<>());

		new Thread(() -> {
			try {
				var result = customerService.register(customer);
				futureRegisteredCustomer.complete(result);
			} catch (Exception e) {
				futureRegisteredCustomer.completeExceptionally(e);
			}
		}).start();
		// Mock
		for (int i=0; i < initialTokens; i++) {
			tokens.add(Token.random());
		}
		assertEquals(initialTokens, tokens.size());

		Event event = publishedEvents.get(id).join();
		assertEquals("CustomerRegistrationRequested", event.getTopic());
		registeredCustomer = event.getArgument(0, Customer.class);
		var correlator = event.getArgument(1, Correlator.class);
		cCorrelators.put(registeredCustomer, correlator);

		correlator = cCorrelators.get(registeredCustomer);
		assertNotNull(correlator);
		var newCustomer = new Customer("Lars", "", "011298-1136", "123", "2");
		customerService.handleCustomerAccountCreated(new Event(EventTypes.CUSTOMER_ACCOUNT_CREATED.getTopic(),
				new Object[] { newCustomer, cCorrelators.get(customer)} ));

		registeredCustomer = futureRegisteredCustomer.join();
	}

	@When("the customer requests {int} tokens")
	public void theCustomerRequestsTokens(int noTokens) {
		mockEvent = new Event(EventTypes.TOKENS_REQUESTED.getTopic(), new Object[]{ registeredCustomer.payId(), noTokens });
		String id = tokenKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		publishedEvents.put(id, new CompletableFuture<>());

		new Thread(() -> {
			var tokenList = tokenService.requestTokens(noTokens,registeredCustomer.payId());
			futureTokenRequest.complete(tokenList);
		}).start();
	}

	@Then("the {string} event is sent asking {int} tokens for that customer id")
	public void theEventIsSentWithTokensForThatCustomerId(String eventType, int noTokens) {
		eventTypeName = EventTypes.fromTopic(eventType);
		String id = tokenKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		Event event = publishedEvents.get(id).join();
		assertEquals(eventTypeName.getTopic(), event.getTopic());
		var customerId = event.getArgument(0, String.class);
		var tokens = event.getArgument(1, Integer.class);
		var correlator = event.getArgument(2, Correlator.class);
		assertEquals(noTokens, tokens.intValue());
		tCorrelators.put(customerId + "-T" + noTokens, correlator);
	}

	@When("the {string} event is received for the same customer with {int} tokens")
	public void theEventIsReceivedForTheSameCustomerWithTokens(String eventType, int noTokens) {
		eventTypeName = EventTypes.fromTopic(eventType);
		var correlator = tCorrelators.get(registeredCustomer.payId() + "-T" + noTokens);
		ArrayList<Token> tokenList = new ArrayList<>();
		for (int i=0; i < noTokens; i++) {
			tokenList.add(Token.random());
		}
		assertNotNull(correlator);
		tokenService.handleTokensGenerated(new Event(eventTypeName.getTopic(), new Object[]{
				tokenList, tCorrelators.get(registeredCustomer.payId() + "-T" + noTokens)}));
	}

	@Then("the customer has {int} valid tokens")
	public void theCustomerHasValidTokens(int noTokens) {
		var tokenList = futureTokenRequest.join();
		assertEquals(noTokens, tokenList.size());
	}

	@Given("a registered customer with id opting to deregister")
	public void aRegisteredCustomerWithTokensOptingToDeregister() {
		customer = new Customer("Lars", "", "011298-1136", "123", "reqid");

		mockEvent = new Event(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ customer.payId() });
		String id = customerIdKeyExtractor.apply(mockEvent, flags.get(mockEvent.getTopic()));

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(customer.payId());
	}

	private CompletableFuture<String> futureCustomerDeregister = new CompletableFuture();
	@When("the customer is being deregistered")
	public void theCustomerIsBeingDeregistered() {
		new Thread(() -> {
			var result = customerIdService.deregister(customer.payId());
			futureCustomerDeregister.complete(result);
		}).start();
	}

	private Map<String, Correlator> cStringCorrelators = new ConcurrentHashMap<>();
	@Then("the {string} event for the customer is sent with their id")
	public void theEventForTheCustomerIsSentWithTheirId(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		String id = customerIdKeyExtractor.apply(mockEvent, flags.get(eventType));

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
		var customerId = futureCustomerDeregister.join();
		assertEquals(customer.payId(), customerId);
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
		 futureMerchantId = futureMerchant.join().payId();
		} catch (CompletionException exception) {
			assertNotNull(exception);
			assertTrue(exception.getCause() instanceof AccountCreationException);
			assertEquals(message, exception.getCause().getMessage());
		}
	}
}

