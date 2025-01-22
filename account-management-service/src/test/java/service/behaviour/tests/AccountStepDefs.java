package service.behaviour.tests;

import dtupay.services.account.AccountManager;
import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.MemoryAccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.utilities.Correlator;
import dtupay.services.account.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AccountStepDefs {

	MessageQueue queue = mock(MessageQueue.class);
	Correlator correlator;
	Customer customer;
	Customer customerNoBank;
	Customer registeredCustomer;
	ArgumentCaptor<Event> eventCaptor;
	ArgumentCaptor<Event> badEventCaptor;
	AccountRepository<Customer> customerAccountRepository = new MemoryAccountRepository<>();
	AccountRepository<Customer> registeredCustomerAccountRepository = new MemoryAccountRepository<>();
	AccountRepository<Merchant> registeredMerchantAccountRepository = new MemoryAccountRepository<>();
	AccountManager accountManagementService = new AccountManager(queue, customerAccountRepository, registeredMerchantAccountRepository);
	AccountManager registeredAccountManagementService;
	Merchant merchant;
	Merchant registeredMerchant;
	EventTypes eventTypeName;

	@When("a {string} event for a customer is received")
	public void aEventForACustomerIsReceived(String arg0) {
		eventTypeName = EventTypes.fromTopic(arg0);
		customer = new Customer("test", "test", "123131-1243", "bank1", null);
		assertNull(customer.payId());
		correlator = Correlator.random();
		accountManagementService.handleCustomerRegistrationRequested(new Event(eventTypeName.getTopic(), new Object[] { customer, correlator }));
	}

	private Event receivedEvent;

	@Then("the {string} event is sent with the same correlation id")
	public void theEventIsSentWithTheSameCorrelationId(String eventName) {
		eventTypeName = EventTypes.fromTopic(eventName);
		if (eventName.contains("Failure") || eventName.contains("Failed")) {
			badEventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(queue).publish(badEventCaptor.capture());
			receivedEvent = badEventCaptor.getValue();
		} else {
			eventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(queue).publish(eventCaptor.capture());
			receivedEvent = eventCaptor.getValue();
		}
		assertEquals(eventTypeName.getTopic(), receivedEvent.getTopic());
		if (eventName.contains("Delete")) {
			assertEquals(correlator.getId(), receivedEvent.getArgument(0, Correlator.class).getId());
		} else{
			assertEquals(correlator.getId(), receivedEvent.getArgument(1, Correlator.class).getId());
		}
	}

	@And("the customer account is assigned a customer id")
	public void theCustomerAccountIsAssignedACustomerId() {
		var recCustomer = receivedEvent.getArgument(0, Customer.class);
		assertEquals(customer.firstName(), recCustomer.firstName());
		assertEquals(customer.lastName(), recCustomer.lastName());
		assertEquals(customer.cpr(), recCustomer.cpr());
		assertEquals(customer.bankAccountNo(), recCustomer.bankAccountNo());
		assertNotNull(recCustomer.payId());
	}

	@When("a {string} event for a customer is received with missing bank account number")
	public void aEventForACustomerIsReceivedWithMissingBankAccountNumber(String arg0) {
		eventTypeName = EventTypes.fromTopic(arg0);
		customerNoBank = new Customer("test", "test", "123131-1243", "", null);
		correlator = Correlator.random();
		accountManagementService.handleCustomerRegistrationRequested(new Event(eventTypeName.getTopic(), new Object[] { customerNoBank, correlator }));
	}

	@And("the customer receives a failure message {string}")
	public void theCustomerReceivesAFailureMessage(String arg0) {
		assertEquals(arg0, receivedEvent.getArgument(0, String.class));
	}

	@When("a {string} event for a merchant is received")
	public void aEventForAMerchantIsReceived(String arg0) {
		eventTypeName = EventTypes.fromTopic(arg0);
		merchant = new Merchant("test", "test", "123456-1234", "1", null);
		assertNull(merchant.payId());
		correlator = Correlator.random();
		accountManagementService.handleMerchantRegistrationRequested(new Event(eventTypeName.getTopic(), new Object[] { merchant, correlator }));
	}

	@And("the merchant account is assigned a merchant id")
	public void theMerchantAccountIsAssignedAMerchantId() {
		var recMerchant = receivedEvent.getArgument(0, Merchant.class);
		assertEquals(merchant.firstName(), recMerchant.firstName());
		assertEquals(merchant.lastName(), recMerchant.lastName());
		assertEquals(merchant.cpr(), recMerchant.cpr());
		assertEquals(merchant.bankAccountNo(), recMerchant.bankAccountNo());
		assertNotNull(recMerchant.payId());
	}

	@Given("a customer stored in the account repository")
	public void aCustomerStoredInTheAccountRepository() {
		customer = new Customer("test", "test", "123131-1243", "bank1", null);
		String Id = registeredCustomerAccountRepository.createAccount(customer);
		registeredAccountManagementService = new AccountManager(queue, registeredCustomerAccountRepository, registeredMerchantAccountRepository);
		registeredCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), Id);
	}

	@Given("a merchant stored in the account repository")
	public void aMerchantStoredInTheAccountRepository() {
		merchant = new Merchant("test", "test", "123131-1243", "bank1", null);
		String Id = registeredMerchantAccountRepository.createAccount(merchant);
		registeredAccountManagementService = new AccountManager(queue, registeredCustomerAccountRepository, registeredMerchantAccountRepository);
		registeredMerchant = new Merchant(merchant.firstName(), merchant.lastName(), merchant.cpr(), merchant.bankAccountNo(), Id);
	}

	@When("a {string} event for the same customer id is received opting to deregister with a correlation id")
	public void aEventForTheSameCustomerIdIsReceivedOptingToDeregisterWithACorrelationId(String eventName) {
		eventTypeName = EventTypes.fromTopic(eventName);
		correlator = Correlator.random();
		assertNotNull(registeredCustomer.payId());
		registeredAccountManagementService.handleCustomerDeregistrationRequested(new Event(eventTypeName.getTopic(), new Object[]{registeredCustomer.payId(), correlator}));
	}

	@When("a {string} event for the same merchant id is received opting to deregister with a correlation id")
	public void aEventForTheSameMerchantIdIsReceivedOptingToDeregisterWithACorrelationId(String eventName) {
		eventTypeName = EventTypes.fromTopic(eventName);
		correlator = Correlator.random();
		assertNotNull(registeredMerchant.payId());
		registeredAccountManagementService.handleMerchantDeregistrationRequested(new Event(eventTypeName.getTopic(), new Object[]{registeredMerchant.payId(), correlator}));
	}

	@When("a {string} event for a customer id is received opting to deregister with a correlation id")
	public void aEventForACustomerIdIsReceivedOptingToDeregisterWithACorrelationId(String eventName) {
		customer = new Customer("test", "test", "123131-1243", "bank1", "1123");
		eventTypeName = EventTypes.fromTopic(eventName);
		correlator = Correlator.random();
		accountManagementService.handleCustomerDeregistrationRequested(new Event(eventTypeName.getTopic(), new Object[]{customer.payId(), correlator}));
	}

	@When("a {string} event for a merchant id is received opting to deregister with a correlation id")
	public void aEventForAMerchantIdIsReceivedOptingToDeregisterWithACorrelationId(String eventName) {
		merchant = new Merchant("test", "test", "123131-1243", "bank1", "1123");
		eventTypeName = EventTypes.fromTopic(eventName);
		correlator = Correlator.random();
		accountManagementService.handleMerchantDeregistrationRequested(new Event(eventTypeName.getTopic(), new Object[]{merchant.payId(), correlator}));
	}
}
