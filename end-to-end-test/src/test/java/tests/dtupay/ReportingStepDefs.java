package tests.dtupay;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import dtupay.model.Customer;
import dtupay.model.Merchant;
import dtupay.model.PaymentRequest;
import dtupay.model.Token;
import dtupay.model.views.CustomerView;
import dtupay.model.views.ManagerView;
import dtupay.model.views.MerchantView;
import dtupay.services.CustomerService;
import dtupay.services.MerchantService;
import dtupay.services.ReportService;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportingStepDefs {
	private CustomerService customerService = new CustomerService();
	private MerchantService merchantService = new MerchantService();
	private Customer registeredCustomer;
	private ReportService reportService = new ReportService();
	private ArrayList<CustomerView> responseCustomer;
	private ArrayList<MerchantView> responseMerchant;
	private ArrayList<ManagerView> responseManager;
	private Merchant registeredMerchant;
	private Exception exception;
	private User user;
	private BankService bankService = new BankServiceService().getBankServicePort();
	private String bankAccountNo;
	private List<String> bankAccounts = new ArrayList<>();
	private Token usedToken;
	private int usedAmount;

	@Given("a customer registered in DTUPay for reporting")
	public void a_customer_registered_in_DTUPay_for_reporting() {
		registeredCustomer = customerService.register(new Customer("John",
					"Sayna", "102030-1234", "9696", "rand"));
	}

	@When("the customer requests a report")
	public void the_customer_requests_a_report() {
		responseCustomer = reportService.getCustomerReport(registeredCustomer.payId());
	}

	@Then("the customer report is retrieved")
	public void the_customer_report_is_retrieved() {
		assertNotNull(responseCustomer);
	}

	@Given("a merchant registered in DTUPay for reporting")
	public void aMerchantRegisteredInDTUPayForReporting() {
		registeredMerchant = merchantService.register(new Merchant("John",
				"Sayna", "102030-1234", "9696", "rand"));
	}

	@When("the merchant requests a report")
	public void theMerchantRequestsAReport() {
		responseMerchant = reportService.getMerchantReport(registeredMerchant.payId());
	}

	@Then("the merchant report is retrieved")
	public void theMerchantReportIsRetrieved() {
		assertNotNull(responseMerchant);
	}

	@When("the manager requests a report")
	public void theManagerRequestsAReport() {
		responseManager = reportService.getManagerReport();
	}

	@Then("the manager report is retrieved")
	public void theManagerReportIsRetrieved() {
		assertNotNull(responseManager);
	}

	ArrayList<Token> customersTokens = new ArrayList<>();
	@Given("a registered customer with tokens")
	public void aRegisteredCustomerWithTokens() throws BankServiceException_Exception {
		user = new User();
		user.setFirstName("George");
		user.setLastName("Bush");
		user.setCprNumber("313131-4444");
		BigDecimal newBalance = new BigDecimal(1000);
		bankAccountNo = bankService.createAccountWithBalance(user, newBalance);
		bankAccounts.add(bankAccountNo);


		try {
			registeredCustomer = customerService.register(new Customer(user.getFirstName(),
					user.getLastName(),
					user.getCprNumber(),
					bankAccountNo, null));
		} catch (Exception e) {
			exception = e;
		}

		customersTokens = customerService.requestTokens(registeredCustomer.payId(), 2);

	}

	@And("a registered merchant")
	public void aRegisteredMerchant() throws BankServiceException_Exception {
		user = new User();
		user.setFirstName("Lars");
		user.setLastName("Hansen");
		user.setCprNumber("101010-1212");
		BigDecimal newBalance = BigDecimal.valueOf(1000);
		bankAccountNo = bankService.createAccountWithBalance(user, newBalance);
		bankAccounts.add(bankAccountNo);
		try {
			registeredMerchant = merchantService.register(new Merchant(user.getFirstName(),
					user.getLastName(),
					user.getCprNumber(),
					bankAccountNo, null));
		} catch (Exception e) {
			exception = e;
		}
	}

	@And("the merchant has requested a payment for {int} from customer")
	public void theMerchantHasRequestedAPaymentFromCustomer(int amount) throws InterruptedException {
		usedToken = customersTokens.get(0);
		usedAmount = amount;
		PaymentRequest paymentRequest = new PaymentRequest(registeredMerchant.payId(), usedToken, usedAmount);
		merchantService.pay(paymentRequest);
		Thread.sleep(100);

	}


	@And("the customer report contains an entry")
	public void customerReportContainsAnEntry() {
		assertNotNull(responseCustomer);
		for (CustomerView customer: responseCustomer){
			assertEquals(registeredMerchant.payId(),customer.getMerchantId());
			assertEquals(usedToken,customer.getToken());
			assertEquals(usedAmount,customer.getAmount());
		}
	}

	@And("the merchant report contains an entry")
	public void theMerchantReportContainsAnEntry() {
		assertNotNull(responseMerchant);
		for (MerchantView merchant: responseMerchant){
			assertEquals(usedToken,merchant.getToken());
			assertEquals(usedAmount,merchant.getAmount());
		}
	}

	@And("the manager report contains an entry")
	public void theManagerReportContainsAnEntry() {
		assertNotNull(responseManager);
		for (ManagerView manager: responseManager){
			assertEquals(usedToken,manager.getToken());
			assertEquals(usedAmount,manager.getAmount());
			assertEquals(registeredCustomer.payId(),manager.getCustomerId());
			assertEquals(registeredMerchant.payId(),manager.getMerchantId());
		}
	}

	@After
	public void after() throws BankServiceException_Exception {
		for (String bankAccountNo : bankAccounts) {
			bankService.retireAccount(bankAccountNo);
		}
	}
}
