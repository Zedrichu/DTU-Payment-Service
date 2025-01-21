package dtupay.services.account;

import dtupay.services.account.annotations.MethodAuthor;
import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.domain.models.PaymentRequest;
import dtupay.services.account.utilities.Correlator;

import dtupay.services.account.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountManager {
	private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);

	private MessageQueue mque;
	private AccountRepository<Customer> customerRepository;
	private AccountRepository<Merchant> merchantRepository;

	public AccountManager(MessageQueue mque, AccountRepository<Customer> customerRepository, AccountRepository<Merchant> merchantRepository) {
		logger.debug("Initializing AccountManagementService");
		this.customerRepository = customerRepository;
		this.merchantRepository = merchantRepository;

		this.mque = mque;

		// Add event handlers
		this.mque.addHandler(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(), this::handleCustomerRegistrationRequested);
		this.mque.addHandler(EventTypes.MERCHANT_REGISTRATION_REQUESTED.getTopic(), this::handleMerchantRegistrationRequested);
		this.mque.addHandler(EventTypes.PAYMENT_INITIATED.getTopic(), this::handlePaymentInitiated);
		this.mque.addHandler(EventTypes.TOKENS_REQUESTED.getTopic(), this::handleTokensRequested);
		this.mque.addHandler(EventTypes.TOKEN_VERIFIED.getTopic(), this::handleTokenVerified);
	}

	public void handleCustomerRegistrationRequested(Event event) {
		logger.debug("Received CustomerRegistrationRequested event: {}", event);
		var customer = event.getArgument(0, Customer.class);
		var correlationId = event.getArgument(1, Correlator.class);

		Event newEvent;
		if (validateAccountInfo(customer)) {
			newEvent = acceptCustomer(customer, correlationId);
			logger.debug("New customer registered: {}", newEvent);
		} else {
			newEvent = declineCustomer(customer, correlationId);
			logger.debug("New customer declined: {}", newEvent);
		}
		this.mque.publish(newEvent);
	}

	public void handleMerchantRegistrationRequested(Event event) {
		logger.debug("Received MerchantRegistrationRequested event: {}", event);
		var merchant = event.getArgument(0, Merchant.class);
		var correlationId = event.getArgument(1, Correlator.class);

		Event newEvent;
		if (validateAccountInfo(merchant)) {
			newEvent = acceptMerchant(merchant, correlationId);
			logger.debug("New merchant registered: {}", newEvent);
		} else {
			newEvent = declineMerchant(merchant, correlationId);
			logger.debug("New merchant declined: {}", newEvent);
		}
		this.mque.publish(newEvent);
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	private Event acceptCustomer(Customer customer, Correlator correlationId) {
		String id = customerRepository.createAccount(customer);
		var registeredCustomer = new Customer(customer.firstName(), customer.lastName(), customer.cpr(), customer.bankAccountNo(), id);
		return new Event(EventTypes.CUSTOMER_ACCOUNT_CREATED.getTopic(), new Object[]{ registeredCustomer, correlationId });
	}

	private Event declineCustomer(Customer customer, Correlator correlationId) {
		return new Event(EventTypes.CUSTOMER_ACCOUNT_CREATION_FAILED.getTopic(), new Object[]{
					"Account creation failed: Provided customer must have a valid bank account number and CPR", correlationId
		});
	}

	private Event acceptMerchant(Merchant merchant, Correlator correlationId) {
		String id = merchantRepository.createAccount(merchant);
		var registeredMerchant = new Merchant(merchant.firstName(), merchant.lastName(), merchant.cpr(), merchant.bankAccountNo(), id);
		return new Event(EventTypes.MERCHANT_ACCOUNT_CREATED.getTopic(), new Object[]{ registeredMerchant, correlationId });
	}

	private Event declineMerchant(Merchant merchant, Correlator correlationId) {
		return new Event(EventTypes.MERCHANT_ACCOUNT_CREATION_FAILED.getTopic(), new Object[]{
				"Account creation failed: Provided merchant must have a valid bank account number and CPR", correlationId
		});
	}

	private boolean validateAccountInfo(Customer customer) {
		boolean cprInvalid = customer.cpr() == null || customer.cpr().isEmpty();
		boolean bankAccountInvalid = customer.bankAccountNo() == null || customer.bankAccountNo().isEmpty();
		return ! (cprInvalid || bankAccountInvalid);
	}
	private boolean validateAccountInfo(Merchant merchant){
		boolean cprInvalid = merchant.cpr() == null || merchant.cpr().isEmpty();
		boolean bankAccountInvalid = merchant.bankAccountNo() == null || merchant.bankAccountNo().isEmpty();
		return ! (cprInvalid || bankAccountInvalid);
	}


	public void handlePaymentInitiated(Event event) {
		Merchant merchant = merchantRepository.getAccount(event.getArgument(0, PaymentRequest.class).merchantId());
		var correlationId = event.getArgument(1, Correlator.class);

		if (!merchantRepository.exists(merchant.payId())) {
			return;
		}

		Event newEvent = new Event(EventTypes.MERCHANT_ACCOUNT_VERIFIED.getTopic(), new Object[]{ merchant, correlationId });
		logger.debug("New merchant verified: {}", newEvent);

		this.mque.publish(newEvent);

	}

    public void handleTokenVerified(Event event) {
		var customerId = event.getArgument(0, String.class);
		Customer customer = customerRepository.getAccount(event.getArgument(0, String.class));
		var correlationId = event.getArgument(1, Correlator.class);

		if (!customerRepository.exists(customerId)) {
			return;
		}

		Event newEvent = new Event(EventTypes.CUSTOMER_ACCOUNT_VERIFIED.getTopic(), new Object[]{ customer, correlationId });
		logger.debug("New customer verified: {}", newEvent);

		this.mque.publish(newEvent);
	}

	public void handleTokensRequested(Event event) {
		logger.debug("Received TokensRequested event: {}", event);
		var customerId = event.getArgument(0, String.class);
		var correlationId = event.getArgument(2, Correlator.class);

		if (!customerRepository.exists(customerId)) {
			return;
		};

		Event newEvent = new Event(EventTypes.TOKEN_ACCOUNT_VERIFIED.getTopic(),
				new Object[]{correlationId});
		this.mque.publish(newEvent);
	}
}

