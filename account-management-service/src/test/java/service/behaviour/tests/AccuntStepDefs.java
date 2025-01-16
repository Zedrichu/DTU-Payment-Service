package service.behaviour.tests;

import dtupay.services.account.AccountManagementService;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.utilities.Correlator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AccuntStepDefs {

	MessageQueue queue = mock(MessageQueue.class);
	AccountManagementService accountManagementService = new AccountManagementService(queue);
	Correlator correlator;
	Customer customer;
	ArgumentCaptor<Event> eventCaptor;

	@When("a {string} event for a customer is received")
	public void aEventForACustomerIsReceived(String arg0) {
		customer = new Customer("test", "test", "123131-1243", "bank1", null);
		assertNull(customer.id());
		correlator = Correlator.random();
		accountManagementService.handleCustomerRegistrationRequested(new Event(arg0, new Object[] { customer, correlator }));
	}

	private Event receivedEvent;

	@Then("the {string} event is sent with the same correlation id")
	public void theEventIsSentWithTheSameCorrelationId(String eventName) {
		eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(queue).publish(eventCaptor.capture());
		receivedEvent = eventCaptor.getValue();
		assertEquals(eventName, receivedEvent.getType());
		assertEquals(correlator.getId(), receivedEvent.getArgument(1, Correlator.class).getId());
	}

	@And("the customer account is assigned a customer id")
	public void theCustomerAccountIsAssignedACustomerId() {
		var recCustomer = receivedEvent.getArgument(0, Customer.class);
		assertEquals(customer.firstName(), recCustomer.firstName());
		assertEquals(customer.lastName(), recCustomer.lastName());
		assertEquals(customer.cpr(), recCustomer.cpr());
		assertEquals(customer.bankAccountNo(), recCustomer.bankAccountNo());
		assertNotNull(recCustomer.id());
	}
}
