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


		Event newEvent;
		if (validateCustomerInfo(customer)) {
			newEvent = acceptCustomer(customer, correlationId);
		} else {
			newEvent = declineCustomer(customer, correlationId);
		}
		this.mque.publish(newEvent);
//		return id;
	}

	private Event acceptCustomer(Customer customer, Correlator correlationId) {
		String id = customerRepository.createAccount(customer);
		var registeredCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), id);
		return new Event("CustomerAccountCreated", new Object[]{ registeredCustomer, correlationId });
	}

	private Event declineCustomer(Customer customer, Correlator correlationId) {
		return new Event("CustomerAccountCreationFailed", new Object[]{
					"Account creation failed: Provided customer must have a valid bank account number and CPR", correlationId
		});
	}

	private boolean validateCustomerInfo(Customer customer) {
		return customer.cpr() != null && customer.bankAccountNo() != null;
	}
}

