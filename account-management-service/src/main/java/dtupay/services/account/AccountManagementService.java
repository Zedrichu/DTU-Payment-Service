package dtupay.services.account;

import dtupay.services.account.domain.AccountMemoryRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.utilities.Correlator;
import messaging.Event;
import messaging.MessageQueue;


public class AccountManagementService {

	private MessageQueue mque;
	private AccountMemoryRepository<Customer> customerRepository = new AccountMemoryRepository<>();

	public AccountManagementService(MessageQueue mque) {
		this.mque = mque;

		// Add event handlers
		this.mque.addHandler("CustomerRegistrationRequested", this::handleCustomerRegistrationRequested);
	}

	public void handleCustomerRegistrationRequested(Event event) {
		var customer = event.getArgument(0, Customer.class);
		var correlationId = event.getArgument(1, Correlator.class);
		String id = customerRepository.createAccount(customer);

		var registeredCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), id);
		Event newEvent = new Event("CustomerAccountCreated", new Object[]{ registeredCustomer, correlationId });
		this.mque.publish(newEvent);
//		return id;
	}
}

