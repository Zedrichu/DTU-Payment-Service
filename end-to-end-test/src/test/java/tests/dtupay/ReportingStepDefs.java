package tests.dtupay;

import dtupay.model.Customer;
import dtupay.model.Merchant;
import dtupay.model.views.CustomerView;
import dtupay.model.views.ManagerView;
import dtupay.model.views.MerchantView;
import dtupay.services.CustomerService;
import dtupay.services.MerchantService;
import dtupay.services.ReportService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.util.ArrayList;

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
}
