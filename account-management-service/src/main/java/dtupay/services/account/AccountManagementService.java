package dtupay.services.account;

import dtupay.services.account.annotations.MethodAuthor;
import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.MemoryAccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.utilities.Correlator;

import messaging.Event;
import messaging.MessageQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountManagementService {
	private static final Logger logger = LoggerFactory.getLogger(AccountManagementService.class);

	private MessageQueue mque;
	private AccountRepository<Customer> customerRepository = new MemoryAccountRepository<>();
	private AccountRepository<Merchant> merchantRepository = new MemoryAccountRepository<>();

	public AccountManagementService(MessageQueue mque) {
		logger.debug("Initializing AccountManagementService");

		this.mque = mque;

		// Add event handlers
		this.mque.addHandler("CustomerRegistrationRequested", this::handleCustomerRegistrationRequested);
		this.mque.addHandler("MerchantRegistrationRequested", this::handleMerchantRegistrationRequested);
	}

	public void handleCustomerRegistrationRequested(Event event) {
		logger.debug("Received CustomerRegistrationRequested event: {}", event);
		var customer = event.getArgument(0, Customer.class);
		var correlationId = event.getArgument(1, Correlator.class);

		Event newEvent;
		if (validateCustomerInfo(customer)) {
			newEvent = acceptCustomer(customer, correlationId);
			logger.debug("New customer registered: {}", newEvent);
		} else {
			newEvent = declineCustomer(customer, correlationId);
			logger.debug("New customer declined: {}", newEvent);
		}
		this.mque.publish(newEvent);
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
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
		boolean cprInvalid = customer.cpr() == null || customer.cpr().isEmpty();
		boolean bankAccountInvalid = customer.bankAccountNo() == null || customer.bankAccountNo().isEmpty();
		return ! (cprInvalid || bankAccountInvalid);
	}

	public void handleMerchantRegistrationRequested(Event event) {
		logger.debug("Received MerchantRegistrationRequested event: {}", event);
		var merchant = event.getArgument(0, Merchant.class);
		var correlationId = event.getArgument(1, Correlator.class);

		String id = merchantRepository.createAccount(merchant);
		var registeredMerchant = new Merchant(merchant.firstName(), merchant.lastName(), merchant.cpr(), merchant.bankAccountNo(), id);
		Event newEvent = new Event("MerchantAccountCreated", new Object[] { registeredMerchant, correlationId });
		logger.debug("New merchant registered: {}", newEvent);

		this.mque.publish(newEvent);
	}
}

